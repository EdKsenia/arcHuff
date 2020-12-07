package huffman;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        try {//указываем инструкцию с помощью аргументов командной строки
            if (args[0].equals("--compress") || args[0].equals("-c")) {
                Hzipping.beginHzipping(args[2], args[4]);
                System.out.println("архивается завершена");
            } else if ((args[0].equals("--extract") || args[0].equals("-x"))) {

                Hunzipping.beginHunzipping(args[2], args[4]);
            } else
                throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            System.out.println("Неверный формат ввода аргументов ");
            System.out.println("Читайте Readme.txt");
            e.printStackTrace();
        }
    }
}

