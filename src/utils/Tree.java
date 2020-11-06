package utils;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private TreeNode<T> root = null;

    public Tree(String identifier, T data) {
        this.root = new TreeNode<T>(identifier, data);
    }

    public Tree(TreeNode<T> root) {
        this.root = root;
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    public TreeNode<T> findNode(String identifier) {
        return findNode(this.root, identifier);
    }

    private TreeNode<T> findNode(TreeNode<T> node,String identifier) {
        if (node == null) {
            return null;
        }

        if (node.getIdentifier().equals(identifier)) {
            return node;
        }

        for (TreeNode<T> child : node.getChildren()
        ) {
            TreeNode<T> result = findNode(child, identifier);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
