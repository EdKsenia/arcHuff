package huffman;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;


public class Hzipping {
    public static final int N = 300;
    static PriorityQueue<Tree> priorityQueue = new PriorityQueue<Tree>();
    static LinkedHashMap<Integer, Integer> freqTable = new LinkedHashMap<Integer, Integer>(); //таблица частот
    static String[] ss = new String[N]; //массив кодов
    static int exbits;
    static byte bt; //переменная для считывания байта
    static int cntUnique; // кол-во уникальных символов
    static ArrayList<Byte> fakezippedArrList = new ArrayList<>(); //вспомогательный лист для кодирования
    static Tree Root;//дерево

    /*******************************************************************************
     * подсчет частот символов в тексте
     ******************************************************************************/
    public static void сalculateFreq(byte[] content) {
        Byte bt;
        for (int i = 0; i < content.length; i++) {
            bt = content[i];
            int freq = 0;
            if (freqTable.containsKey(to(bt)))
                freq = freqTable.get(to(bt));
            freqTable.put(to(bt), freq + 1);
        }
    }

    /***********************************************************************************
     * проверка и преобразование отрицательного байта,
     * с возвращением вместо него целочисленного элемента
     ***********************************************************************************/
    public static int to(Byte b) {
        int ret = b;
        if (ret < 0) {
            ret = ~b;
            ret = ret + 1;
            ret = ret ^ 255;
            ret += 1;
        }
        return ret;
    }

    /**********************************************************************************
     * освобождение памяти
     *********************************************************************************/
    public static void initHzipping() {
        int i;
        cntUnique = 0;
        if (Root != null)
            freeDfs(Root);
        for (i = 0; i < N; i++) {
            freqTable.put(i, 0);
        }
        for (i = 0; i < N; i++)
            ss[i] = "";
        priorityQueue.clear();
        fakezippedArrList.clear();
    }

    /**********************************************************************************
     * DFS для освобождения памяти дерева
     *********************************************************************************/
    public static void freeDfs(Tree now) {
        if (now.leftChild == null && now.rightChild == null) {
            now = null;
            return;
        }
        if (now.leftChild != null)
            freeDfs(now.leftChild);
        if (now.rightChild != null)
            freeDfs(now.rightChild);
    }

    /**********************************************************************************
     * DFS для создания кодовых слов
     *********************************************************************************/
    public static void dfs(Tree now, String st) {
        now.deb = st;
        if ((now.leftChild == null) && (now.rightChild == null)) {
            ss[now.bite] = st;
            return;
        }
        // построение кодового слова
        //левый ребенок - 0
        //правый ребенок - 1
        if (now.leftChild != null)
            dfs(now.leftChild, st + "0");
        if (now.rightChild != null)
            dfs(now.rightChild, st + "1");
    }

    /*******************************************************************************
     * Создание узлов(листьев) для каждого символа из текста,
     * задание приоритетов в очереди создание дерева
     *******************************************************************************/
    public static void MakeNode() {
        int i;
        priorityQueue.clear();
        for (i = 0; i < N; i++) {
            if (freqTable.get(i) != 0) {
                //создание объекта Дерева(Tree) для каждого из узлов
                Tree temp = new Tree();
                //Вставить эти деревья в приоритетную очередь.
                //Чем меньше частота, тем больше приоритет.
                // Таким образом, при извлечении всегда выбирается дерво наименьшей частотой.
                temp.bite = i;
                temp.freqnc = freqTable.get(i);
                temp.leftChild = null;
                temp.rightChild = null;
                priorityQueue.add(temp);
                cntUnique++;
            }
        }
        Tree temp1, temp2;

        if (cntUnique == 0) {
            return;
        } else if (cntUnique == 1) {
            for (i = 0; i < N; i++)
                if (freqTable.get(i) != 0) {
                    ss[i] = "0";
                    break;
                }
            return;
        }

        //Извлекаем два дерева из приоритетной очереди(temp1 и temp2) и
        // сделать их потомками нового узла (только что созданного узла без буквы).
        // Частота нового узла равна сумме частот двух деревьев-потомков.
        while (priorityQueue.size() != 1) {
            Tree temp = new Tree();
            temp1 = priorityQueue.poll();
            temp2 = priorityQueue.poll();
            temp.leftChild = temp1;
            temp.rightChild = temp2;
            temp.freqnc = temp1.freqnc + temp2.freqnc;
            priorityQueue.add(temp);
        }
        Root = priorityQueue.poll();
    }

    /*******************************************************************************
     * создание листа, в котором хранятся окончательные бинарные коды
     * для архивирования файла
     *******************************************************************************/
    public static void fakezip(byte[] content) {

        for (int j = 0; j < content.length; j++) {
            String code = ss[to(content[j])];
            String[] local = code.split("");
            ArrayList<Byte> localByte = new ArrayList<>();
            for (int k = 0; k < local.length; k++) {
                byte elem = (local[k].equals("0")) ? (byte) 0 : (byte) 1;
                localByte.add(k, elem);
            }
            fakezippedArrList.addAll(localByte);
        }
    }

    /*******************************************************************************
     * создание заархивированного файла в соотвтствии с кодами в листе freqTable
     *******************************************************************************/
    public static void realzip(String fname1) {
        File fileo;
        int i = 10;
        Byte btt;
        //сжатый файл
        fileo = new File(fname1);

        try {
            FileOutputStream file_output = new FileOutputStream(fileo);
            DataOutputStream data_out = new DataOutputStream(file_output);
            //В начале файла хранится информация для последующей дешифрации
            //количество уникальных символов
            data_out.writeInt(cntUnique);
            for (i = 0; i < 256; i++) {
                if (freqTable.get(i) != 0) {
                    //данные из таблицы частот символов
                    btt = (byte) i;
                    data_out.write(btt);
                    data_out.writeInt(freqTable.get(i));
                }
            }
            long texbits;
            texbits = fakezippedArrList.size() % 8;
            texbits = (8 - texbits) % 8;
            exbits = (int) texbits;
            data_out.writeInt(exbits);
            //архивация
            try {
                for (int k = 0; k < fakezippedArrList.size(); k++) {
                    bt = 0;
                    byte ch;
                    for (exbits = 0; exbits < 8 && k < fakezippedArrList.size(); exbits++) {
                        ch = fakezippedArrList.get(k);
                        k++;
                        bt *= 2;
                        if (ch == 1) {
                            bt++;
                        }
                    }
                    k--;
                    data_out.write(bt);
                }
            } catch (EOFException eof) {
                int x;
                if (exbits != 0) {
                    for (x = exbits; x < 8; x++) {
                        bt *= 2;
                    }
                    data_out.write(bt);
                }

                exbits = (int) texbits;
                System.out.println("extrabits: " + exbits);
                System.out.println("End of File");

            }
            data_out.close();
            file_output.close();
            System.out.println("output file's size: " + fileo.length());
        } catch (IOException e) {
            System.out.println("IO exception = " + e);
        }
    }

    //основной метод для архивации
    public static void beginHzipping(String arg1, String arg2) {

        //подготовка данных
        initHzipping();

        //запись исходного файла в массив байтов
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(Paths.get(arg1));
        } catch (IOException e) {
            System.out.println("Mistake in reading file");
        }

        //подсчет частот всех символов
        сalculateFreq(content);
        System.out.println("Finished calculating");

        //Создание узлов(листьев) для каждого символа из текста
        MakeNode();
        System.out.println("Finished making note");

        //Если количество уникальных элементов больше 1,
        //то создаем коды для символов
        if (cntUnique > 1)
            dfs(Root, ""); // dfs to make the codes

        long start1 = System.currentTimeMillis();

        // создание вспомогательной строки для кодирования символов
        fakezip(content);

        long finish1 = System.currentTimeMillis();
        long timeConsumedMillis1 = finish1 - start1;
        System.out.println("Finished create fakezip");
        long start2 = System.currentTimeMillis();

        //создание заархивированного файла
        realzip(arg2 + ".huffz"); // making the real zip

        long finish2 = System.currentTimeMillis();
        long timeConsumedMillis2 = finish2 - start2;

        //освобождение памяти
        initHzipping();

        System.out.println("Time fakezip: " + timeConsumedMillis1);
        System.out.println("Time realzip: " + timeConsumedMillis2);
    }
}