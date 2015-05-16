package ru.ifmo.ctddev.tenischev.translation.second;

import org.StructureGraphic.v1.DSChildren;
import org.StructureGraphic.v1.DSValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kris13 on 07.05.15.
 */
public class Tree {
    @DSValue
    public String node;
    @DSChildren(DSChildren.DSChildField.ITERABLE)
    public List<Tree> children;

    public Tree(String node, Tree... children) {
        this.node = node;
        this.children = Arrays.asList(children);
    }

    public Tree(String node) {
        this.node = node;
        children = new ArrayList<>();
    }
}
