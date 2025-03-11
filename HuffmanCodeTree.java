import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HuffmanCodeTree {
    
    // the root of the Huffman code tree
    private TreeNode root;

    /**
     * Create a new empty Huffman code tree.
     */
    public HuffmanCodeTree() {
        root = null;
    }

    /**
     * Build a Huffman code tree using the bit-sequences and their frequencies
     * pre: frequencies != null
     * @param freqs the mapping of the bit-sequences and their corresponding frequencies
     */
    public HuffmanCodeTree(int[] frequencies) {
        // check preconditions
        if (frequencies == null) {
            throw new IllegalArgumentException("frequencies cannot be null.");
        }
        
        FairPriorityQueue<TreeNode> pq = new FairPriorityQueue<>();
        // create new nodes with bit-sequence and frequency and add to queue
        for (int i = 0; i < IHuffConstants.ALPH_SIZE + 1; i++) {
            // bit-sequence exists in the file
            if (frequencies[i] > 0) {
                pq.add(new TreeNode(i, frequencies[i]));
            }
        }
        // build the code tree and combine nodes until only one node remains
        while (pq.size() > 1) {
            TreeNode merged = new TreeNode(pq.removeFirst(), -1, pq.removeFirst());
            pq.add(merged);
        }
        root = pq.removeFirst();
    }

    /**
     * Create the codes from the Huffman code tree
     * @return a mapping of the bit-sequences and their corresponding code
     */
    public Map<Integer, String> treeToCode() {
        // create and add coding by traversing the tree
        Map<Integer, String> huffCodings = new HashMap<>();
        traverseTree(root, huffCodings, "");
        return huffCodings;
    }
    
    /**
     * Traverse the Huffman code tree and generate codes for each bit-sequence
     * @param node the current node to look at
     * @param map the bit-sequences and their corresponding code
     * @param code the code of the bit-sequence
     */
    private void traverseTree(TreeNode node, Map<Integer, String> map, String code) {
        // base case, leaf node
        if (node.isLeaf()) {
            map.put(node.getValue(), code);
        } else {
            // recursive cases, traverse left and right sub trees
            traverseTree(node.getLeft(), map, code + "0");
            traverseTree(node.getRight(), map, code + "1");
        }
    }

    /**
     * Do a pre order traversal on Huffman code tree and write bits
     * @param bitsOut is bound to a file/stream to which bits are written
     * for the compressed file
     */
    public void writeTree(BitOutputStream bitsOut) {
        // write bits by doing a pre order traversal
        writeTreeHelp(root, bitsOut);
    }
    
    /**
     * Helper method for writeTree to recurse
     * @param node the current node
     * @param bitsOut is bound to a file/stream to which bits are written
     * for the compressed file
     */
    private void writeTreeHelp(TreeNode node, BitOutputStream bitsOut) {
        // base case, node is a leaf node
        if (node.isLeaf()) {
            // write bits for the leaf node and the actual data
            bitsOut.writeBits(1, 1);
            bitsOut.writeBits(1 + IHuffConstants.BITS_PER_WORD, node.getValue());
        } else {
            // write bits for internal node
            bitsOut.writeBits(1, 0);
            // traverse through remaining tree
            writeTreeHelp(node.getLeft(), bitsOut);
            writeTreeHelp(node.getRight(), bitsOut);
        }
    }
    
    /**
     * Get the number of bits of the Huffman code tree
     * @param node the current node
     * @return the total bits of the Huffman code tree
     */
    public int treeBits() {
        return bitsHelp(root);
    }
    
    /**
     * Helper for treeBits to recurse
     * @param node the current node
     * @return the total bits of the Huffman code tree
     */
    private int bitsHelp(TreeNode node) {
        // base case, node is a leaf node
        if (node.isLeaf()) {
            // add bits for the node and the actual value contained inside of it
            return 1 + (1 + IHuffConstants.BITS_PER_WORD);
        } else {
            // add bit for each node and traverse the tree
            return 1 + bitsHelp(node.getLeft()) + bitsHelp(node.getRight());
        }
    }
    
    /**
     * Rebuild the Huffman code tree based on compressed data
     * @param in is the previously compressed data
     * @throws IOException if an error occurs while reading from the input file
     */
    public void rebuildTree(BitInputStream in) throws IOException {
        // rebuild the tree
        root = rebuildHelp(in, root);
    }
    
    /**
     * Helper method for rebuildTree to recurse
     * @param in is the previously compressed data
     * @param node the current node to write for
     * @return a TreeNode that is the new root of the tree
     * @throws IOException if an error occurs while reading from the input file
     */
    private TreeNode rebuildHelp(BitInputStream in, TreeNode node) throws IOException {
        // current node is a leaf node, so create new node with corresponding value
        if (in.readBits(1) == 1) {
            node = new TreeNode(in.readBits(1 + IHuffConstants.BITS_PER_WORD), 1);
        } else {
            // create an internal node
            node = new TreeNode(-1, 1);
            // build remaining tree
            node.setLeft(rebuildHelp(in, node.getLeft()));
            node.setRight(rebuildHelp(in, node.getRight()));
        }
        return node;
    }
    
    /**
     * Walk the tree and read bit by bit
     * @param bitsIn is the previously compressed data
     * @param bitsOut is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file
     * @throws IOException if an error occurs while reading from the input file
     */
    public int walkTree(BitInputStream bitsIn, BitOutputStream bitsOut) throws IOException {
        // get ready to walk tree
        TreeNode curNode = root;
        int writtenBits = 0;
        int bit = bitsIn.readBits(1);
        boolean done = false;
        while (!done) {
            // PEOF value was never read
            if (bit == -1) {
                throw new IOException("Error reading compressed file. \n" +
                        "unexpected end of input. No PSEUDO_EOF value.");
            }
            if (bit == 0) {
                // move left in tree
                curNode = curNode.getLeft();
            } else {
                // move right in tree
                curNode = curNode.getRight();
            }
            // reached a leaf node
            if (curNode.isLeaf()) {
                // stop since PEOF has been reached
                if (curNode.getValue() == IHuffConstants.PSEUDO_EOF) {
                    done = true;
                } else {
                    // write the bits for the current node value
                    bitsOut.writeBits(IHuffConstants.BITS_PER_WORD, curNode.getValue());
                    writtenBits += IHuffConstants.BITS_PER_WORD;
                }
                // reset node reference to the root
                curNode = root;
            }
            // read next bit
            bit = bitsIn.readBits(1);
        }
        return writtenBits;
    }
}
