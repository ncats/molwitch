**W**hich **I**nternal **T**oolkit for **CH**emicals

MolWitch is a Bridge Pattern wrapper around various cheminformatics toolkit
so that users can change the underlying toolkit at runtime without changing
any client code.

For example using Chemaxon's Jchem if someone has a license but allow others
without to use the open source CDK.

#Available on Maven Central
Usually, one needs to add 2 dependencies:
This adds the API.
```
<dependency>
  <groupId>gov.nih.ncats</groupId>
  <artifactId>molwitch</artifactId>
  <version>0.5.8</version>
</dependency>
```

There also needs to be a molwitch implementation

To add CDK:
```
<dependency>
        <groupId>gov.nih.ncats</groupId>
        <artifactId>molwitch-cdk</artifactId>
        <version>1.0.3</version>
</dependency>
```