/*
 * Copyright (C) 2015 Yannis Papanikolaou
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.auth.csd.mlkd.mlclassification.homer;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yannis Papanikolaou
 * @param <T>
 */
public class Node<T> implements Serializable {

    private final T data;
    private final Node<T> parent;
    private ArrayList<Node<T>> children;

    private final int id;
    private Labels metaLabels = null;
    private Dictionary dictionary;
    
    private int depth=-1;
    private double silhouette=-2;
    

    public Labels getMetaLabels() {
        return metaLabels;
    }

    public void setMetaLabels(Labels metaLabels) {
        this.metaLabels = metaLabels;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setSilhouette(double silhouette) {
        this.silhouette = silhouette;
    }

    public int getDepth() {
        return depth;
    }

    public double getSilhouette() {
        return silhouette;
    }

    public Node(T data, Node<T> parent, ArrayList<Node<T>> children, int id, Tree tree) {
        this.data = data;
        this.parent = parent;
        this.children = children;
        if (children != null) {
            for (Node<T> child : children) {
                tree.getNodeMap().put("L" + child.getId(), child);
            }
        }
        this.id = id;
    }

    public void setChildren(ArrayList<Node<T>> children, Tree tree) {
        this.children = children;
        Tree.setNumberOfNodes(Tree.getNumberOfNodes() + children.size());
        for (Node<T> child : children) {
            tree.getNodeMap().put("L" + child.getId(), child);
        }

    }

    public int getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public Node<T> getParent() {
        return parent;
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public void print(Labels labels) {
        print("", true, labels);
    }

    private void print(String prefix, boolean isTail, Labels labels) {

        System.out.println(prefix + (isTail ? "└── " : "├── ") + printData(labels));
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false, labels);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ? "    " : "│   "), true, labels);
        }
    }

    private String printData(Labels labels) {
        //return data.toString();
        TIntIterator it = ((TIntHashSet) data).iterator();
        String s = "";
        while (it.hasNext()) {
            s += labels.getLabel(it.next()) + "--";
        }
        return s;
    }

    void setDictionary(Dictionary dictionaryPerNode) {
        this.dictionary = dictionaryPerNode;
    }
    
}
