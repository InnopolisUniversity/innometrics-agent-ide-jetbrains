package ru.innopolis.university.test;

public class WithLambdaExpr {

    public static void main(String[] args) {
        System.out.println();
        new Thread(() -> System.out.println("lambda"));
    }
}
