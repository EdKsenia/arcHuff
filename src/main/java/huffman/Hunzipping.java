package huffman;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;

public class Hunzipping {
    static PriorityQueue<Tree> pq1 = new PriorityQueue<Tree>();
    static int[] freq1 = new int[300];
    static LinkedHashMap<Integer, Integer> freqTable = new LinkedHashMap<Integer, Integer>();
    static String[] ss1 = new String[300]; //массив кодов
    static String[] btost = new String[300]; // INT TO BIN
    static String bigone; // строка, из которой выделяются кодовые слова
    static String temp;
    static int exbits1; // биты, добавляемые в конец файла, чтобы кол-во элементов было кратно 8
    static int putit; //
    static int cntu; // кол-во уникальных элементов

    static Tree root;

    /**********************************************************************************
     * Освобождение памяти
     *********************************************************************************/
    public static void initHunzipping() {
        int i;
        if (root != null)
            freeDfs1(root);
        for (i = 0; i < 300; i++)
            freqTable.put(i, 0);
        for (i = 0; i < 300; i++)
            ss1[i] = "";
        pq1.clear();
        bigone = "";
        temp = "";
        exbits1 = 0;
        putit = 0;
        cntu = 0;
    }

    /**********************************************************************************
     * DFS для освобождения памяти дерева
     *********************************************************************************/
    public static void freeDfs1(Tree now) {
        if (now.leftChild == null && now.rightChild == null) {
            now = null;
            return;
        }
        if (now.leftChild != null)
            freeDfs1(now.leftChild);
        if (now.rightChild != null)
            freeDfs1(now.rightChild);
    }

    /**********************************************************************************
     * DFS для создания кодовых слов
     *********************************************************************************/
    public static void dfs1(Tree now, String st) {
        now.deb = st;
        if ((now.leftChild == null) && (now.rightChild == null)) {
            ss1[now.bite] = st;
            return;
        }
        // построение кодового слова
        //левый ребенок - 0
        //правый ребенок - 1
        if (now.leftChild != null)
            dfs1(now.leftChild, st + "0");
        if (now.rightChild != null)
            dfs1(now.rightChild, st + "1");
    }

    /*******************************************************************************
     * Создание узлов(листьев) для каждого символа из текста,
     * задание приоритетов в очереди создание дерева
     *******************************************************************************/
    public static void MakeNode1() {
        int i;
        cntu = 0;
        for (i = 0; i < 300; i++) {
            if (freqTable.get(i) != 0) {
                //создание объекта Дерева(Tree) для каждого из узлов
                Tree Temp = new Tree();
                //Вставить эти деревья в приоритетную очередь.
                //Чем меньше частота, тем больше приоритет.
                // Таким образом, при извлечении всегда выбирается дерво наименьшей частотой.
                Temp.bite = i;
                Temp.freqnc = freqTable.get(i);
                Temp.leftChild = null;
                Temp.rightChild = null;
                pq1.add(Temp);
                cntu++;
            }

        }
        Tree Temp1, Temp2;

        //инициализация листа частот
        if (cntu == 0) {
            return;
        } else if (cntu == 1) {
            for (i = 0; i < 300; i++)
                if (freqTable.get(i) != 0) {
                    ss1[i] = "0";
                    break;
                }
            return;
        }

        //Извлекаем два дерева из приоритетной очереди(temp1 и temp2) и
        // сделать их потомками нового узла (только что созданного узла без буквы).
        // Частота нового узла равна сумме частот двух деревьев-потомков.
        while (pq1.size() != 1) {
            Tree Temp = new Tree();
            Temp1 = pq1.poll();
            Temp2 = pq1.poll();
            Temp.leftChild = Temp1;
            Temp.rightChild = Temp2;
            Temp.freqnc = Temp1.freqnc + Temp2.freqnc;
            pq1.add(Temp);
        }
        root = pq1.poll();
    }

    /*******************************************************************************
     * чтение информации для дешифрации файла.
     * в начале заархивированного файла находится вспомогательная информация
     ******************************************************************************/
    public static void readfreq1(byte[] content) {
        int fey, i;
        Byte baital;
        int b1 = to(content[0]);
        int b2 = to(content[1]);
        int b3 = to(content[2]);
        int b4 = to(content[3]);
        cntu = getInt(b1, b2, b3, b4);
        for (int j = 4; j < cntu * 5 + 4; j += 5) {
            baital = content[j];
            b1 = to(content[j + 1]);
            b2 = to(content[j + 2]);
            b3 = to(content[j + 3]);
            b4 = to(content[j + 4]);
            fey = getInt(b1, b2, b3, b4);
            freqTable.put(to(baital), fey);
            freqTable.put(to(baital), fey);
        }

        MakeNode1();
        if (cntu > 1)
            dfs1(root, "");

        for (i = 0; i < 256; i++) {
            if (ss1[i] == null)
                ss1[i] = "";
        }
    }

    /***********************************************************************************
     * конвертация в бинарную строку кодов
     ***********************************************************************************/
    public static void createbin() {
        int i, j;
        String t;
        for (i = 0; i < 256; i++) {
            btost[i] = "";
            j = i;
            while (j != 0) {
                if (j % 2 == 1)
                    btost[i] += "1";
                else
                    btost[i] += "0";
                j /= 2;
            }
            t = "";
            for (j = btost[i].length() - 1; j >= 0; j--) {
                t += btost[i].charAt(j);
            }
            btost[i] = t;
        }
        btost[0] = "0";
    }

    /******************************************************************************
     * got проверяет валидность кода
     ******************************************************************************/
    public static int got() {
        int i;

        for (i = 0; i < 256; i++) {
            if (ss1[i].compareTo(temp) == 0) {
                putit = i;
                return 1;
            }
        }
        return 0;

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

    /***********************************************************************************
     * преобразование любой сроки в восьмизначную строку
     ***********************************************************************************/
    public static String makeeight(String b) {
        String ret = "";
        int i;
        int len = b.length();
        for (i = 0; i < (8 - len); i++)
            ret += "0";
        ret += b;
        return ret;
    }

    //преобразование четырех элементов из массива в целочисленный элемент
    public static int getInt(int b1, int b2, int b3, int b4) {
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
    }

    /***********************************************************************************
     * функция распаковки
     **************************************************************************************/
    public static void readbin(byte[] content, String unz) {
        File f2 = null;
        int ok, bt;
        Byte b;
        int j, i;
        bigone = "";
        f2 = new File(unz);
        try {
            FileOutputStream file_output = new FileOutputStream(f2);
            DataOutputStream data_out = new DataOutputStream(file_output);
            // начинаем с этого элемента, так как в начале файла
            // содержится информация для распаковки
            int b1 = to(content[cntu * 5 + 4]);
            int b2 = to(content[cntu * 5 + 5]);
            int b3 = to(content[cntu * 5 + 6]);
            int b4 = to(content[cntu * 5 + 7]);
            exbits1 = getInt(b1, b2, b3, b4);
            for (int k = cntu * 5 + 8; k < content.length; k++) {
                b = content[k];
                bt = to(b);
                bigone += makeeight(btost[bt]);
                while (true) {
                    ok = 1;
                    temp = "";
                    for (i = 0; i < bigone.length() - exbits1; i++) {
                        temp += bigone.charAt(i);
                        if (got() == 1) {
                            data_out.write(putit);
                            ok = 0;
                            String s = "";
                            for (j = temp.length(); j < bigone.length(); j++) {
                                s += bigone.charAt(j);
                            }
                            bigone = s;
                            break;
                        }
                    }

                    if (ok == 1)
                        break;
                }
            }
            file_output.close();
            data_out.close();

        } catch (IOException e) {
            System.out.println("IO Exception =: " + e);
        }
    }

    /************************************************************************************
    * основной метод для распаковки файла
    **************************************************************************************/

    public static void beginHunzipping(String arg1, String arg2) {
        initHunzipping();
        byte[] content = new byte[0];
        try {
            content = Files.readAllBytes(Paths.get(arg1));
        } catch (IOException e) {
            System.out.println("Mistake in reading file");
        }
        long start1 = System.currentTimeMillis();
        readfreq1(content);
        long finish1 = System.currentTimeMillis();
        long timeConsumedMillis1 = finish1 - start1;
        System.out.println("Finished read freq" + timeConsumedMillis1);
        start1 = System.currentTimeMillis();
        createbin();
        finish1 = System.currentTimeMillis();
        timeConsumedMillis1 = finish1 - start1;
        System.out.println("Finished create bin" + timeConsumedMillis1);
        int n = arg1.length();
//        arg2 = arg1.substring(0, n - 6);
        start1 = System.currentTimeMillis();
        readbin(content, arg2);
        finish1 = System.currentTimeMillis();
        timeConsumedMillis1 = finish1 - start1;
        System.out.println("Finished read bin" + timeConsumedMillis1);
        initHunzipping();
    }
}