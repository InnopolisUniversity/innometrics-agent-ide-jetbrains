package ru.innopolis.university.test;

public class WithInnerClasses {
    class Inner {
        private void method() {
            System.out.println(new First() {
                public void methodInAnonClass() {
                    System.out.println("anon class");
                }
            });
        }
    }
}
