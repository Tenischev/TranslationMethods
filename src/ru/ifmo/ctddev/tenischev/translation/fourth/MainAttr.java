package ru.ifmo.ctddev.tenischev.translation.fourth;

import org.StructureGraphic.v1.DSutils;
import ru.ifmo.ctddev.tenischev.translation.fourth.gen.Parser;
import ru.ifmo.ctddev.tenischev.translation.fourth.gen.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by kris13 on 05.06.15.
 */
public class MainAttr {
    public static void main(String[] args) {
        new MainAttr();
    }

    private MainAttr() {
        try (FileInputStream fileInputStream = new FileInputStream(new File(".", "expression2.in"))){
            Tree tree = (new Parser()).parse(fileInputStream);
            if (tree instanceof Tree.start) {
                Tree.start start = (Tree.start) tree;
                System.out.println(start.val);
            }
            DSutils.show(tree, 120, 40);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }
}
