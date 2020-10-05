**W**hich **I**nternal **T**oolkit for **CH**emicals
# MolWitch
MolWitch is a Bridge Pattern wrapper around various Cheminformatics Toolkits
so that users can change the underlying toolkit at runtime without changing
any client code.


## Available on Maven Central
Usually, one needs to add multiple dependencies for bgoth `molwitch` as well as the underlying toolkit:
This adds the API.
```
<dependency>
  <groupId>gov.nih.ncats</groupId>
  <artifactId>molwitch</artifactId>
  <version>0.6.0</version>
</dependency>
```

There also needs to be a molwitch implementation

To add [molwitch-CDK](https://github.com/ncats/molwitch-cdk) :
```
<dependency>
        <groupId>gov.nih.ncats</groupId>
        <artifactId>molwitch-cdk</artifactId>
        <version>1.0.4</version>
</dependency>
```

## License 
This project is Open Sourced under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0) 
However the underlying molwitch implementations may use different licences depending on the license
of the underlying toolkit.

