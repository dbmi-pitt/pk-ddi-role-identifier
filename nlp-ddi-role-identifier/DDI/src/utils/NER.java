package utils;

import java.util.logging.Level;
import martin.common.ArgParser;
import martin.common.Loggers;
import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.matching.Matcher;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Chinh
 */
public class NER {
    public static Matcher createDic(String path){
        Boolean b = Boolean.valueOf("true");
        ArgParser ap = new ArgParser(new String[]{"--variantMatcher", path, "--ignoreCase", b.toString()});
        java.util.logging.Logger log = Loggers.getDefaultLogger(ap);
        log.setLevel(Level.SEVERE);
        Matcher matcher = EntityTagger.getMatcher(ap, log);
        return matcher ;
    }

}
