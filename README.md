IAACalculator V2


The IAACalculator calculates the iaa score for two or more annotator's
using the Percentage-, RandolphKappa- or Bennett's agreement.


Changes made in V2:
My Brother David Machajewski gave the idea to create an annotator class which
represents an annotator, since my old approach to iterate over lists without
saving specific positions increased the complexity and readability of the code.

That idea lead to rewriting the first version, since
the first version was poorly written and unstructured.

The overall structure of V2:-----------

-Extractor
		-extracts all relevant information from the tsv, like the amount of annotator's,
		which verbs are used and an mapping of a verb to a List that holds all sentences with that
		verb as the annotation verb.
		Furthermore, a mapping of the verb to its senses and a mapping of sentenceIDs to their row.
	
All the information extracted from the tsv and saved inside the extractor 
are used inside an IAACalulator- and IAAFactory object.

-IAACalculator
	- provides methods to calculate the number of senses per verb, the number of sentences per verb
	 and methods to calculate the iaa for annotator pairs or for an iaa calculation over all rows
	 of a given list with String[] of form
	 [satzID, verb, satz, annotator1,....annotatorN, null]
	-Additionally, it provides methods for calculating the correlation between two 
	variables X,Y. (Used to calculate corr for the number of sentences per verb 
	and the number of senes per verb or the iaa per verb and senses per verb.
	
	
-IAAFactory
	- uses methods from the IAACalculator class and all annotator's from the AnnotatorManager
	 to calculate the pairwise iaa for all verbs or the pairwise iaa over all sentences.
	-It also calculates the iaa for all annotator over all rows or over all rows of a given verb.

-Annotator
	- an annotator knows his column, his name and satzIDs(not implemented yet)
	- knowing his column is very useful, if we work with lists which contain rows
	with the parsed form, because if we iterate over all rows an annotator can
	return his column position, which can be used to get the annotation he made at the given sentence

-AnnotatorManager
	-holds the Annotator objects	 

-IAAExporter 
	-exports the results from the IAAExtractor,IAACalculator and IAAFactory
	
-Show	
	-useful functions to print entries from a list of string[] to the console

----------------------------------------------------------------------------------------
	
Infos:
The basic tsv file that contains the data must have the following 
structure or it won't work:

[sentenceID  verb  sentence  Annotator1  ......  AnnotatorN] (AnnotatorN folllowed by a tab)

(the current tsv contains a null column after the AnnotatorN column)

Furthermore it takes into account, that annotator's didn't
annotate all sentences equally, which results in a "*" at the specific cell
that should contain a sense number like "784421"(see below).

[id-s10-0	sagen	Gesagt, alle Mann und Frau getan.	12345	*	*	*	*	*	*	54321	*	]


The DKPro package is used to calculate the iaa scores.
The "*" were changed to null values, via parsing, to match the needs addItem function
inside the CodingAnnotationStudy class.
E.g
[id-s10-0	sagen	Gesagt, alle Mann und Frau getan.	12345	null	null	null	null	null	null	54321	null	]


It should be mentioned that the Bennett's agreement is only for
2 Annotators and Randolph Kappa has a range from -1 to +1
since its a correlation statistic.

Cohens kappa (after Jacob Cohen 1960) interpretation:
Interpretation of Cohen’s kappa.\

 <table style="width:100%">
  <tr>
    <th>Value of Kappa</th>
    <th>Level of Agreement</th>
    <th> % of Data that are Reliable</th>
  </tr>
  <tr>
    <td>0–.20</td>
    <td>None</td>
    <td>0–4%</td>
  </tr>
  <tr>
    <td>.21–.39	 </td>
    <td>Minimal</td>
    <td>4–15%</td>
  </tr>
  <tr>
    <td>.40–.59	 </td>
    <td>Weak</td>
    <td>15–35%</td>
  </tr>
  <tr>
    <td>.60–.79	 </td>
    <td>Moderate</td>
    <td>35–63%</td>
  </tr>
  <tr>
    <td>.80–.90	 </td>
    <td>Strong</td>
    <td>64–81%</td>
  </tr>
  <tr>
    <td>Above.90</td>
    <td> Almost Perfect</td>
    <td>82–100%</td>
  </tr>
</table> 

![step1](https://raw.githubusercontent.com/MartinJewski/IAACalculatorV2/master/pictures/step1.png?raw=true)<br/>
![step2](https://raw.githubusercontent.com/MartinJewski/IAACalculatorV2/master/pictures/step2.png?raw=true)<br/>
![step3](https://raw.githubusercontent.com/MartinJewski/IAACalculatorV2/master/pictures/step3.png?raw=true)<br/>
![step4](https://raw.githubusercontent.com/MartinJewski/IAACalculatorV2/master/pictures/step4.png?raw=true)<br/>


Creators Martin and David Machajewski.
