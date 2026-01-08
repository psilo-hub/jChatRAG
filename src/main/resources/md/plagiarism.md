# Plagiarism Checker

## Overview

The **Plagiarism Checker** is a feature that is currently under development. The concept is inspired by a  
previous project and aims to provide a robust tool for detecting potential instances of plagiarism within  
documents. While the feature is not yet fully implemented, the following outlines the envisioned  
functionality and workflow.

## Key Features

### Document Folder Specification

- **Homework Folder**: Users can specify a folder containing documents that need to be checked for plagiarism.  
This folder will be referred to as the "homework folder."
  
- **Additional Documents Folder**: Users can also designate an additional folder containing more documents.  
These documents will serve as a reference pool for comparison against the homework documents.

### Document Comparison Process

1. **Initial Comparison**: Each document in the homework folder is compared against all other documents  
within the homework folder and the additional documents folder.

2. **Similarity Metrics**: The comparison process utilizes various metrics to calculate similarity, including:
   - **Longest Common Substring**: Identifies the longest sequence of characters common to both documents.
   - **Levenshtein Distance**: Measures the minimum number of single-character edits required to change one  
   document into another.
   - **Embedding Vector Distance**: Compares the semantic similarity of documents using vector representations.

3. **Flagging Plagiarism**: Documents in the homework folder that exhibit a high degree of similarity to  
another document are flagged as potential plagiarism cases. The most similar document is noted for further review.

### Web Search Integration

- **Web Search for Additional Sources**: For documents that do not exhibit high similarity within the  
provided folders, a web search is performed to gather additional potential sources.
  
- **Secondary Comparison**: The remaining homework documents are then compared against the results of the  
web search. This step helps to identify any instances of plagiarism that may originate from external sources.

## Feedback and Suggestions

Your feedback is crucial in shaping the future of this feature. If you have any suggestions or ideas on how to  
improve the Plagiarism Checker, please do not hesitate to share them.  
Your insights will help us create a more effective and user-centric tool.