package huffman;

//бинарное дерево
public class Tree implements Comparable<Tree> {
    Tree leftChild;
    Tree rightChild;
    public String deb;
    public int bite;
    public int freqnc;//частота

    public int compareTo(Tree T) {
        if (this.freqnc < T.freqnc)
            return -1;
        if (this.freqnc > T.freqnc)
            return 1;
        return 0;
    }
}

