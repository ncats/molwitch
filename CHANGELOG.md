# Molwitch API Changelog
## 0.6.3
1. refactored InchiKeyBag into an interface and made Default and Binary implementations

## 0.6.2
1. added new CtTable Clean Rule M  SAL lines with atom positions &lt; 1 are removed from the atom list
1. MolSearcherFactory now returns an `Optional<MolSearcher>` to support not finding a searcher. Previously would throw NoSuchElementException.
1. added new InchiKey object with helper methods to do exact vs inexact matching.
1. log4j dependency updates
1. removed jni-inchi dependency and now use jna-inchi which bumps inchi version from 1.03 to 1.06
1. added direct mol file to inchi support.
## 0.6.1
 - internal version only
## 0.6.0
1. removed `Atom#getRadicalValue()` since we also have `Atom#getRadical()`
1. more improvements to CTFileCleaner now includes atom block cleanup
1. First work on `ChemicalDataStore`
1. Added a few more methods to `ChemcialReaderFactory` with format paramater
1. Added toadded `ChemicalImplFactory#createFromString(format, input)` changed some other ChemicalFactory
   methods to use that instead.
1. Changed some of the `Chemical.parse()` methods to use TextLineParser to read input as String including EOLs
1. Changed some of the `Chemical.parse(File )` methods to use InputStreamSupplier so we can take compressed files
1. added `ChemicalWriterImplFactory#writeAsString` for performance improvements when we want to write out just as a String

## 0.5.9
1. fixed spacing in CTFileCleaner STY lines which broke JSdraw

## 0.5.8
1. CTFileCleaner enhancements to add clean up for DAT Sgroups

## 0.5.7
1. CTFileCleaner bug fixes




## 0.5.2

1. Added MolSearch and MolSearchFactory to do GraphIsoMorphism searches.
2.  Bug fixes
