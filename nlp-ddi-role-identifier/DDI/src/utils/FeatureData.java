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

package utils;

/**
 *
 * @author Chinh
 */


import java.io.Serializable;
import java.util.TreeMap;
import libsvm.svm_node;


public class FeatureData implements Serializable {
    public String id="";
    double label=0;
    public static boolean skip_norm = false;
    private TreeMap<Integer, Node> data = new TreeMap<>();
    public static class Node extends svm_node implements Comparable<Node>, Serializable {

        public Node(int index, double value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int compareTo(Node o) {
            if (index < o.index) {
                return -1;
            } else if (index > o.index) {
                return 1;
            }
            return 0;
        }
    }

    public FeatureData(int capacity) {
        
    }

    public void setLabel(double value){
        label = value;
    }
    public double  getLabel(){
        return label ;
    }
    
    public FeatureData() {
        
    }
public void add(int index, double value) {
        add(new Node(index, value));
    }

    public void add(Node e) {
        if (data.containsKey(e.index)) {
            Node node = data.get(e.index);
            node.value += e.value;
        } else {
            data.put(e.index, e);
        }
    }

    public svm_node[] getData() {
        return (svm_node[]) data.values().toArray(new Node[0]);
    }

    public int size() {
        return data.size();
    }

    /**
     * Normalize feature values
     */
    public void normalize() {
        double sum = 0;
        for (Node node : data.values()) {
            sum += Math.pow(node.value, 2);
        }
        sum = Math.sqrt(sum);
        for (Node node : data.values()) {
            node.value /= sum;
        }
    }

    /**
     * Merge feature vectors into single vector
     * @param list of sparse vectors
     * @param fsize: feature vector sizes
     * @return: merged vector
     */
    public static FeatureData mergeVector(FeatureData[] list, int fsize[]) {
        FeatureData new_fd = new FeatureData();
        for (FeatureData fd : list) {
            if (fd != null && fd.size() > 0) {
                fd.normalize();
            }
        }
        int idx = 0;
        // merge all feature data into single vector
        for (int i = 0; i < list.length; i++) {
            if (list[i] == null) {
                idx += fsize[i];
                continue;
            }
            for (Node e : list[i].data.values()) {
                if (e != null) {
                    e.index += idx;
                    new_fd.add(e);
                }
            }
            idx += fsize[i];
        }
        return new_fd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label==1?1:-1);
        sb.append(' ');
        for (Node e : data.values()) {
            sb.append(e.index).append(":").append(e.value).append(" ");
        }
        sb.append('\n');
        return sb.toString();
    }

    public static void main(String[] args) {
        
        
    }
}

