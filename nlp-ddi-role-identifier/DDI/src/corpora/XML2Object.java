/*
 * Copyright 2014 Chinh Bui.
 * Email: bqchinh@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package corpora;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import nlp.SenData;
import nlp.ShallowParser;
import nlp.Word;
import relation.DDIPair;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;
import utils.Data;
import utils.NER;

/**
 *
 * @author Chinh
 */
public class XML2Object {

    Matcher matcher = NER.createDic(Data.Trigger_path); // default trigger list

    public void setTriggers(String path) {
        matcher = NER.createDic(path); // path to trigger list
    }

    public Document loadCorpus(File path) {
        try {
            // First create a new XMLInputFactory
            Document corpus;
            JAXBContext context = JAXBContext.newInstance(Document.class);
            try (FileReader reader = new FileReader(path)) {
                Unmarshaller um = context.createUnmarshaller();
                corpus = (Document) um.unmarshal(reader);
                return corpus;
            } catch (JAXBException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    List<String> getArgList(String txt) {
        java.util.regex.Matcher m = p.matcher(txt);
        List<String> list = new ArrayList<>();
        while (m.find()) {
            list.add(m.group()); // creating a list of proteins from text ;
        }
        return list;
    }
    Pattern p = Pattern.compile("DRUG\\d{1,3}");

    public void saveData(String path, String source[], boolean train) {
        try {
            parser.initParser();
            Map<String, SenData> senMap = new TreeMap<>();
            for (String s : source) {
                System.out.println("Source:\t" + s);
                long t1 = System.currentTimeMillis();
                File f = new File(s);
                File[] files = f.listFiles();
                for (File file : files) {
                    Document doc = loadCorpus(file);
                    List<Sentence> sens = doc.getSentence();
                    for (Sentence sen : sens) {
                        SenData dt = preparedData(sen, train);
                        senMap.put(dt.senID, dt);
                    }
                }
                long t2 = System.currentTimeMillis();
                System.out.println("Total time:\t" + (t2 - t1) / 1000);
            }
            try {
                System.out.println("---> Writing sendata to disk ...." + senMap.size() + " objects ...");
                Data.write(path, senMap);
                System.out.println("Saving data ... done!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveTestData(String path, String source[], boolean train, Map<String, Boolean> map) {
        parser.initParser();
        Map<String, SenData> senMap = new TreeMap<>();
        for (String s : source) {
            File f = new File(s);
            File[] files = f.listFiles();
            for (File file : files) {
                Document doc = loadCorpus(file);
                List<Sentence> sens = doc.getSentence();
                for (Sentence sen : sens) {
                    SenData dt = preparedData(sen, train);
                    for (DDIPair pair : dt.ddiList) {
                        String key = pair.arg1.id + pair.arg2.id;
                        if (map.containsKey(key)) {
                            pair.ddi = map.get(key);
                        } else {
                            key = pair.arg2.id + pair.arg1.id;
                            if (map.containsKey(key)) {
                                pair.ddi = map.get(key);
                            } else {
                                System.out.println("No answer found for pair:\t" + pair.id);
                            }
                        }
                    }
                    senMap.put(dt.senID, dt);
                }
            }
        }
        try {
            System.out.println("---> Writing sendata to disk ...." + senMap.size() + " objects ...");
            Data.write(path, senMap);
            System.out.println("Saving data ... done!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Word> detectRelWord(String txt) {
        List<Word> list = new ArrayList<>();
        List<Mention> ls = matcher.match(txt);
        String w;
        int locs[];
        for (Mention m : ls) {
            locs = new int[2];
            w = m.getText();
            locs[0] = m.getStart();
            locs[1] = m.getEnd();
            Word tg = new Word("", w, locs);
            tg.type = m.getIds()[0];
            list.add(tg);
        }
        return list;
    }

    private String senSimplify(String txt, List<Word> ls) {
        StringBuilder sb = new StringBuilder(txt);
        Word w = null;
        int idx = ls.size();
        try {
            for (int i = ls.size() - 1; i >= 0; i--) {
                w = ls.get(i);
                w.name = "DRUG" + idx;
                if (sb.substring(w.locs[0], w.locs[1]).length() == w.word.length()) { // equal
                    sb.replace(w.locs[0], w.locs[1], w.name);
                } else if (sb.length() >= w.locs[1] + 1 && sb.substring(w.locs[0], w.locs[1] + 1).length() == w.word.length()) {//+1
                    sb.replace(w.locs[0], w.locs[1] + 1, w.name);
                } else if (sb.length() >= w.locs[1] + 2 && sb.substring(w.locs[0], w.locs[1] + 2).length() == w.word.length()) { //+2
                    sb.replace(w.locs[0], w.locs[1] + 2, w.name);
                } else if (w.locs[0] >= 0 && w.locs[1] > w.locs[0] && sb.length() >= w.locs[1] && sb.substring(w.locs[0], w.locs[1] - 1).length() == w.word.length()) {//-1
                    sb.replace(w.locs[0], w.locs[1] - 1, w.name);
                } else {
                    sb.replace(w.locs[0], w.locs[1] + 1, w.name); // trainning data
                }
                idx--;
            }
        } catch (Exception ex) {
            System.out.println("Unknown cases ---> :\t" + txt);
            if (w != null) {
                System.out.println(w.word + "\tLoc:\t" + w.locs[0] + "\t" + w.locs[1]);
            }
        }
        while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == '\n' || sb.charAt(sb.length() - 1) == ' ')) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    ShallowParser parser = new ShallowParser();

    private List<DDIPair> createDDIPair(Sentence sens, boolean train) {
        List<DDIPair> ddiList = new ArrayList<>();
        Word d1, d2, temp;
        for (Pair pr : sens.getPair()) {
            d1 = drugMap.get(pr.getE1());
            d2 = drugMap.get(pr.getE2());
            if (d1.locs[0] > d2.locs[0]) { // d1 > d2 , swap
                temp = d1;
                d1 = d2;
                d2 = temp;
            }
            boolean ddi = false;
            if (train) {
                String s_ddi = pr.getDdi();
                if (s_ddi != null) {
                    ddi = s_ddi.equals("true");
                } else {
                    s_ddi = pr.getInteraction();
                    if (s_ddi != null) {
                        ddi = s_ddi.equals("true");
                    }
                }
            }
            String type = ddi ? pr.getType() : "";
            DDIPair pair = new DDIPair(pr.getId(), d1, d2, type, ddi);
            ddiList.add(pair);
        }
        return ddiList;
    }
    Map<String, Word> drugMap = new HashMap<>();

    private List<Word> createDrugList(List<Entity> list) {
        List<Word> ls = new ArrayList<>();
        drugMap.clear();
        for (Entity en : list) {
            String offset = en.getCharOffset();
            String values[] = offset.split("-|;");
            int locs[] = new int[2];
            locs[0] = Integer.parseInt(values[0]);
            locs[1] = Integer.parseInt(values[1]);
            Word dr = new Word(en.getId(), en.getText(), locs);
            dr.pos = -1;
            dr.type = en.getType();
            ls.add(dr);
            drugMap.put(dr.id, dr);
        }
        Collections.sort(ls);
        return ls;
    }

    public SenData preparedData(Sentence sens, boolean train) {
        List<Word> drugList = createDrugList(sens.getEntity());
        List<Word> connectList = new ArrayList<>();
        List<Word> negList = new ArrayList<>();
        List<Word> prepList = new ArrayList<>();
        List<Word> ccList = new ArrayList<>();
        List<Word> commaList = new ArrayList<>();
        String text = senSimplify(sens.getText(), drugList).trim();
        SenData sen = new SenData(text, 0, sens.getId());
        sen.long_text = sens.getText();
        sen.relList = detectRelWord(sen.text);
        sen.drugList = drugList;
        parser.initSen(sen);
        int idx_dr = 0;
        int r_idx = 0;
        List<Word> rList = sen.relList;
        for (int i = 0; i < sen.tokens.length; i++) {
            List<String> drList = getArgList(sen.tokens[i]);
            if (drList.size() > 0) {
                for (String name : drList) {
                    drugList.get(idx_dr).pos = i;
                    idx_dr++;
                }
            }
            if (r_idx < rList.size()) {
                if (sen.tokens[i].equals(rList.get(r_idx).word)) {
                    Word trg = rList.get(r_idx);
                    trg.pos = i;
                    trg.posTag = sen.POS[i];
                    r_idx++;
                } else if (sen.tokens[i].contains("-")) {
                    if (sen.tokens[i].endsWith("-" + rList.get(r_idx).word)) {
                        Word trg = rList.get(r_idx);
                        trg.pos = i;
                        trg.posTag = sen.POS[i];
                        r_idx++;
                    }
                    if (r_idx < rList.size() && sen.tokens[i].startsWith(rList.get(r_idx).word + "-")) {
                        Word trg = rList.get(r_idx);
                        trg.pos = i;
                        trg.posTag = sen.POS[i];
                        r_idx++;
                    }
                }
            }
            if (Data.connnectSet.contains(sen.tokens[i].toLowerCase())) {
                Word cond_word = new Word(sen.tokens[i].toLowerCase(), i, null);
                connectList.add(cond_word);
            }
            if (Data.negSet.contains(sen.tokens[i])) {
                Word cond_word = new Word(sen.tokens[i], i, null);
                negList.add(cond_word);
            }
            if (Data.prepSet.contains(sen.tokens[i])) {
                Word cond_word = new Word(sen.tokens[i], i, null);
                prepList.add(cond_word);
            }
            if (Data.ccSet.contains(sen.tokens[i])) {
                Word cond_word = new Word(sen.tokens[i], i, null);
                ccList.add(cond_word);
            }
            if (sen.tokens[i].equals(",")) {
                Word com_word = new Word(sen.tokens[i], i, null);
                commaList.add(com_word);
            }
        }
        sen.connector = connectList;
        sen.relList = rList;
        sen.prepList = prepList;
        sen.negList = negList;
        sen.chunks = parser.parse(sen);
        sen.ddiList = createDDIPair(sens, train);
        sen.ccList = ccList;
        sen.commaList = commaList;
        Collections.sort(sen.ddiList);
        sen.init = true;
        return sen;
    }

    public Map<String, SenData> readData(String path) {

        try {
            Map<String, SenData> senMap = (Map<String, SenData>) Data.read(path);
            return senMap;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
