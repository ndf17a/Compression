
/***************************
* Program Name: Huffman.java
* Description: Compress a file from uncompressed.txt to compressed.txt.hh then expand it into uncompressed.txt
* Name: Nicolas Fornicola
* Date: 11/11/2020
* Course: CS 375.01 Software Engineering II 
* Compile Instructions: javac Huffman.java
* Execute: java Huffman a.txt
*
***************************/
import java.io.File;
import java.io.IOException;


public class Huffman {

    private static final String sl = File.separator;

    // alphabet size of extended ASCII
    private static final int R = 256;
    public static boolean logging = true;

    // Huffman trie node
    private static class Node implements Comparable<Node> {
        private final char ch;
        private final int freq;
        private final Node left, right;

        Node(char ch, int freq, Node left, Node right) {
            this.ch    = ch;
            this.freq  = freq;
            this.left  = left;
            this.right = right;
        }

        // is the node a leaf node?
        private boolean isLeaf() {
            assert (left == null && right == null) || (left != null && right != null);
            return (left == null && right == null);
        }

        // compare, based on frequency
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }
    }


    // compress bytes from standard input and write to standard output
    public static void compress(BinaryIn in, BinaryOut out) {
        // read the input
        //String s = BinaryStdIn.readString();
        String s = in.readString();
        char[] input = s.toCharArray();

        // tabulate frequency counts
        int[] freq = new int[R];
        for (int i = 0; i < input.length; i++)
            freq[input[i]]++;

        // build Huffman trie
        Node root = buildTrie(freq);
       
        // build code table
        String[] st = new String[R];
        buildCode(st, root, "");

        // print trie for decoder
        writeTrie(root, out);

        // print number of bytes in original uncompressed message
        out.write(input.length);

        // use Huffman code to encode input
        for (int i = 0; i < input.length; i++) {
            String code = st[input[i]];
            for (int j = 0; j < code.length(); j++) {
                if (code.charAt(j) == '0') {
                    out.write(false);
                }
                else if (code.charAt(j) == '1') {
                    out.write(true);
                }
                else throw new RuntimeException("Illegal state");
            }
        }

        // flush output stream
        out.flush();
    }

    // build the Huffman trie given frequencies
    private static Node buildTrie(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        for (char i = 0; i < R; i++)
            if (freq[i] > 0)
                pq.insert(new Node(i, freq[i], null, null));

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left  = pq.delMin();
            Node right = pq.delMin();
            Node parent = new Node('\0', left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        return pq.delMin();
    }


    // write bitstring-encoded trie to standard output
    private static void writeTrie(Node x, BinaryOut out) {
        if (x.isLeaf()) {
            out.write(true);
            out.write(x.ch);
            return;
        }
        out.write(false);

        writeTrie(x.left, out);
        writeTrie(x.right, out);
    }

    // make a lookup table from symbols and their encodings
    private static void buildCode(String[] st, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(st, x.left,  s + '0');
            buildCode(st, x.right, s + '1');
        }
        else {
            st[x.ch] = s;
        }
    }


    // expand Huffman-encoded input from standard input and write to standard output
    public static void expand(BinaryIn comIn, BinaryOut comOut) {

        // read in Huffman trie from input stream
        Node root = readTrie(comIn, comOut); 

        // number of bytes to write
        int length = comIn.readInt();

        // decode using the Huffman trie
        for (int i = 0; i < length; i++) {
            Node x = root;
            while (!x.isLeaf()) {
                boolean bit = comIn.readBoolean();
                if (bit) x = x.right;
                else     x = x.left;
            }
            comOut.write(x.ch);
        }
        comOut.flush();
    }


    private static Node readTrie(BinaryIn comIn, BinaryOut comOut) {

        boolean isLeaf = comIn.readBoolean();
        if (isLeaf) {
	    char x = comIn.readChar();
	       
            return new Node(x, -1, null, null);
        }
        else {
            return new Node('\0', -1, readTrie(comIn, comOut), readTrie(comIn, comOut));
        }
    }


    public static void main(String[] args) throws IOException
    {
        if(args.length == 0)
        {
            System.err.println("Need at least one file to be compressed");
            return;
        }  

        for(int i = 0; i < args.length; i++)
        {
            //path to the output folder with the compressed and expanded version
            String outputpath = args[i];
    
            //make the folder
            File file = new File(args[i]);

            if(file.exists())
            {
                 //make and input stream from a.txt -> a.txt.hh
                BinaryIn  in  = new BinaryIn(args[i]);
                BinaryOut out = new BinaryOut(outputpath + ".hh");

                if(file.length() != 0)
                {
                    //compress from a.txt -> a.txt.hh
                    compress(in, out);
                }
                else
                    System.err.println("File to be compressed is empty.\n"); 
                
                //close the output stream inbetween files so it can be swapped from a -> b
                out.close();
  
            }
            else
                System.err.println("File to be compressed does not exist.\n"); 

        }
        
        
    }

}

