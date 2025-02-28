# Huffman
Huffman coding is an algorithm for generating a coding tree for a given piece of data that produces a provably minimal encoding for a given pattern of letter frequencies.
* In order to support variable-length encodings for data, we must use prefix coding schemes, which can be modeled as binary trees Huffman coding constructs encodings by building a tree from the bottom-up, putting the most frequent characters higher up in the coding tree.
* Huffman coding constructs encodings by building a tree from the bottom-up, putting the most frequent characters higher up in the coding tree.
* We must send a header with information to reconstruct the tree with the encoded message so that it can be decoded.

Huffman Coding Algorithm: 
1. Scan the file to be compressed and build a frequency table that tallies the number of times each value appears.
2. Initialize an empty priority queue that will hold partial trees.
3. Create one leaf node per distinct value and add each leaf node to
the queue where the priority is the frequency of the value.
4. While there are two or more trees in the priority queue:
    1. Dequeue the two lowest-priority trees.
    2. Combine them together to form a new tree whose priority is the sum of the priorities of the two trees.
    3. Add that tree back to the priority queue.
5. Traverse the tree to create the encoding table.
6. Scan the file again to create a new compressed file using the Huffman codes.

