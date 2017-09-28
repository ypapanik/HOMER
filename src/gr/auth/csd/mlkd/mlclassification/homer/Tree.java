/*
 * Copyright (C) 2015 Yannis Papanikolaou <ypapanik@csd.auth.gr>
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 * @param <T>
 */
public class Tree<T> implements Serializable {
    private static int numberOfNodes=0;
    private HashMap<String, Node<T>> nodeMap = new HashMap<>();

    public static int getNumberOfNodes() {
        return numberOfNodes;
    }

    public static void setNumberOfNodes(int numberOfNodes) {
        Tree.numberOfNodes = numberOfNodes;
    }

    public static Tree readTree(String treeFile) {
        Tree hierarchy = null;
        if (treeFile == null) {
            System.out.println("No treeFile specified!");
            return null;
        }
        try (final ObjectInputStream input = new ObjectInputStream(new FileInputStream(treeFile))) {
            hierarchy = (Tree) input.readObject();
        } catch (Exception ex) {
            Logger.getLogger(Tree.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hierarchy;
    }
    private final Node<T> root;

    public Tree(T rootData) {
        root = new Node<>(rootData, null, new ArrayList<Node<T>>(), 0, null);
        nodeMap.put("L"+0, root);
    }

    public Node<T> getRoot() {
        return root;
    }

    public void writeTree(String treeFile) {
        try (final ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(treeFile))) {
            output.writeObject(this);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public HashMap<String, Node<T>> getNodeMap() {
        return nodeMap;
    }
    
    
}