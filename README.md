**W**hich **I**nternal **T**oolkit for **CH**emicals
# MolWitch
MolWitch is a Bridge Pattern wrapper around various Cheminformatics Toolkits
so that users can change the underlying toolkit at runtime without changing
any client code.


## Available on Maven Central
Usually, one needs to add 2 dependencies:
This adds the API.
```
<dependency>
  <groupId>gov.nih.ncats</groupId>
  <artifactId>molwitch</artifactId>
  <version>0.6.0</version>
</dependency>
```

There also needs to be a molwitch implementation

To add CDK:
```
<dependency>
        <groupId>gov.nih.ncats</groupId>
        <artifactId>molwitch-cdk</artifactId>
        <version>1.0.4</version>
</dependency>
```