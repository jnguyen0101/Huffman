# Huffman Coding
Huffman coding is an algorithm for generating a coding tree for a given piece of data that produces a provably minimal encoding for a given pattern of letter frequencies.

## Features

- Frequency analysis of input characters
- Huffman tree construction
- Generation of prefix-free binary codes
- Encoding of original input using Huffman codes
- Decoding of encoded binary string
- Optional: File I/O support for compression/decompression

## How It Works

1. **Frequency Count**: Count how often each character appears in the input
2. **Build Huffman Tree**: Use a priority queue to build a binary tree, combining the least frequent characters first
3. **Generate Codes**: Traverse the tree to assign binary codes to each character
4. **Encode**: Replace each character in the input with its corresponding Huffman code
5. **Decode**: Use the tree to translate binary codes back to the original input
