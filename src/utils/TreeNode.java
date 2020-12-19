package utils;

import java.util.ArrayList;
import java.util.List;

public class TreeNode<T> {
    private T data = null;
    private String identifier;
    private List<TreeNode<T>> children = new ArrayList<>();
    private TreeNode<T> parent = null;

    public TreeNode(String identifier, T data) {
        this.data = data;
        this.identifier = identifier;
    }

    public void addChild(TreeNode<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChildren(List<TreeNode<T>> children) {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }

    public void clearChildren(){
        this.children.clear();
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    private void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

}

