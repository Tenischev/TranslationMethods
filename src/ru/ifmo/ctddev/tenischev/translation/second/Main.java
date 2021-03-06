package ru.ifmo.ctddev.tenischev.translation.second;

import org.StructureGraphic.v1.DSutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by kris13 on 07.05.15.
 */
public class Main {
    public static void main(String[] args) {
        new Main();
    }

    private Main() {
        try (FileInputStream fileInputStream = new FileInputStream(new File(".", "expression.in"))){
            Tree tree = (new Parser()).parse(fileInputStream);
            DSutils.show(tree, 60, 40);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }
}
