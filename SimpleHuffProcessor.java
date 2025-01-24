/*  Student information for assignment:
 *
 *  On MY honor, Jenny Nguyen, this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1
 *  UTEID: jtn2497
 *  email address: nguyenjenny01012006@gmail.com
 *  Grader name: Lauren
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private Map<Integer, Integer> frequencies; // frequencies of bit-sequences
    private int[] freqArray; // frequencies as an array
    private Map<Integer, String> huffCodings; // Huffman codings of bit-sequences
    private HuffmanCodeTree tree; // Huffman code tree
    private int savedBits; // number of bits saved by compression
    private int compressedBits; // number of bits compressed
    private int hFormat; // header format
    
    /**
     * Create a mapping of the file's bit-sequences and their frequencies
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @return a map that represents the bit-sequences and their frequencies
     * @throws IOException if an error occurs while reading from the input file
     */
    private Map<Integer, Integer> getFreqs(InputStream in) throws IOException {
        // create new frequency map and array
        Map<Integer, Integer> freqs = new TreeMap<>();
        int[] fArray = new int[ALPH_SIZE + 1];
        BitInputStream bits = new BitInputStream(in);
        int nextBits = bits.readBits(BITS_PER_WORD);
        // read all bit-sequences of the file
        while (nextBits != -1) {
            if (!freqs.containsKey(nextBits)) {
                // create new key and value if bit-sequence doesn't exist
                freqs.put(nextBits, 1);
            } else {
                // if it does exist, increment corresponding frequency
                freqs.put(nextBits, freqs.get(nextBits) + 1);
            }
            // add frequencies to array as well
            fArray[nextBits]++;
            nextBits = bits.readBits(BITS_PER_WORD);
        }
        // add PEOF value to frequency array
        fArray[ALPH_SIZE] = 1;
        freqArray = fArray;
        bits.close();
        return freqs;
    }
    
    /**
     * Create the frequencies based on the file, the Huffman code tree, and the
     * codings based on the Huffman code tree
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @throws IOException if an error occurs while reading from the input file
     */
    private void process(InputStream in) throws IOException {
        // get frequencies and add PEOF value
        frequencies = getFreqs(in);
        frequencies.put(PSEUDO_EOF, 1);
        
        // create the Huffman code tree
        tree = new HuffmanCodeTree(freqArray);
        
        // get map of Huffman codings
        huffCodings = tree.treeToCode();
    }

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        // build the Huffman code tree, count frequencies, build code from tree
        process(in);
        
        // get number of bits before and after compression
        int originalBits = getOGBits();
        compressedBits = getCompressedBits();
        
        // add magic number, format constant, and PEOF value
        compressedBits += BITS_PER_INT * 2;
        compressedBits += huffCodings.get(PSEUDO_EOF).length();
        
        // add number of bits based on header format
        if (headerFormat == STORE_COUNTS) {
            compressedBits += ALPH_SIZE * BITS_PER_INT;
        } else if (headerFormat == STORE_TREE) {
            compressedBits += tree.treeBits() + BITS_PER_INT;
        }
        
        hFormat = headerFormat;
        // calculate number of bits saved
        savedBits = originalBits - compressedBits;
        return savedBits;
    }
    
    /**
     * Get the number of bits before compression
     * @return the number of original bits
     */
    private int getOGBits() {
        int bits = 0;
        // calculate bits based on frequencies
        for (int seq : frequencies.keySet()) {
            // skip over PEOF value
            if (seq != PSEUDO_EOF) {
                bits += frequencies.get(seq) * BITS_PER_WORD;
            }
        }
        return bits;
    }
    
    /**
     * Get the number of bits after compression
     * @return the number of compressed bits
     */
    private int getCompressedBits() {
        int bits = 0;
        // calculate bits based on Huffman codings
        for (int seq : huffCodings.keySet()) {
            // skip over PEOF value
            if (seq != PSEUDO_EOF) {
                String s = huffCodings.get(seq);
                bits += frequencies.get(seq) * s.length();
            }
        }
        return bits;
    }

    /**
     * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        // check preconditions, make sure preprocessCompress has been called and number of bits
        // in output file isn't greater than number of bits in input file
        if ((savedBits >= 0 || force) && huffCodings.size() != 0) {
            BitOutputStream bitsOut = new BitOutputStream(out);
            
            // write bits for magic number and format constant
            bitsOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            bitsOut.writeBits(BITS_PER_INT, hFormat);
            
            // write bits based on header format
            if (hFormat == STORE_COUNTS) {
                for(int i = 0; i < ALPH_SIZE; i++) {
                    bitsOut.writeBits(BITS_PER_INT, freqArray[i]);
                }
            } else if (hFormat == STORE_TREE) {
                // write bits for size of tree and do pre order traversal 
                bitsOut.writeBits(BITS_PER_INT, tree.treeBits());
                tree.writeTree(bitsOut);
            } else {
                // format is something else so show error
                myViewer.showError("Error reading input file. \n" +
                        "unknown header format read.");
                bitsOut.close();
                return -1;
            }
            
            // write bits for actual data
            BitInputStream bitsIn = new BitInputStream(in);
            writeActualData(bitsIn, bitsOut);

            // write bits for PEOF
            writeBitsFromString(huffCodings.get(PSEUDO_EOF), bitsOut);
            bitsIn.close();
            bitsOut.close();
            return compressedBits;
        }
        
        // show error if compressed file is larger and force is not true
        myViewer.showError("Compressed file has " + Math.abs(savedBits) + " more bits than "
                + "uncompressed file. \nSelect \"force compression\" option to compress.");
        return -1;
    }
    
    /**
     * Write the data from file into compressed bits.
     * @param in is the stream being compressed
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    private void writeActualData(BitInputStream in, BitOutputStream out) throws IOException {
        int nextBits = in.readBits(BITS_PER_WORD);
        while (nextBits != -1) {
            writeBitsFromString(huffCodings.get(nextBits), out);
            nextBits = in.readBits(BITS_PER_WORD);
        }
    }
    
    /**
     * Write bits from Huffman codings
     * @param code the coding to write bits
     * @param bitsOut is bound to a file/stream to which bits are written
     * for the compressed file
     */
    private void writeBitsFromString(String code, BitOutputStream bitsOut) {
        for (int i = 0; i < code.length(); i++) {
            // write bits based on value of Huffman code
            if (code.charAt(i) == '1') {
                bitsOut.writeBits(1, 1);
            } else {
                bitsOut.writeBits(1, 0);
            }
        }
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        BitInputStream compBits = new BitInputStream(in);
        freqArray = new int[ALPH_SIZE + 1];
        int magic = compBits.readBits(BITS_PER_INT);
        // make sure the file starts with the magic number
        if (magic != MAGIC_NUMBER) {
            myViewer.showError("Error reading compressed file. \n" +
                    "File did not start with the huff magic number.");
            compBits.close();
            return -1;
        }
        
        // read format constant
        hFormat = compBits.readBits(BITS_PER_INT);
        // read header format
        if (hFormat == STORE_COUNTS) {
            // rebuild frequency array
            for(int i = 0; i < ALPH_SIZE; i++) {
                int compFreq = compBits.readBits(BITS_PER_INT);
                freqArray[i] = compFreq;
            }
            // add PEOF
            freqArray[ALPH_SIZE] = 1;
            // rebuild tree based on frequencies read
            tree = new HuffmanCodeTree(freqArray);
        } else if (hFormat == STORE_TREE) {
            // skip over the number of bits in tree
            compBits.readBits(BITS_PER_INT);
            // set tree to a temp tree and rebuild
            tree = new HuffmanCodeTree();
            tree.rebuildTree(compBits);
        }
        
        // read the actual data
        BitOutputStream bitsOut = new BitOutputStream(out);
        int writtenBits = decode(compBits, bitsOut);
        
        compBits.close();
        bitsOut.close();
        return writtenBits;
    }
    
    /**
     * Walk the tree and read bit by bit
     * @param bitsIn is the previously compressed data
     * @param bitsOut is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file
     * @throws IOException if an error occurs while reading from the input file
     */
    private int decode(BitInputStream bitsIn, BitOutputStream bitsOut) throws IOException {
        // write bits and get number of bits written
        int writtenBits = tree.walkTree(bitsIn, bitsOut);
        return writtenBits;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    private void showString(String s) {
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
