# Blueprint Vizualizer

Draw your blueprint beans!

## Usage

### Generate image from one file

```
./run.sh blueprintFile.xml
```

### Generate image from many files

```
./run.sh blueprintFile.xml other.xml ...
```

### Generate image from many files without grouping by file

```
./run.sh blueprintFile.xml other.xml --no-group-by-file
```

### Generate image with exclude beans

```
./run.sh blueprintFile.xml -e myBean1 -e myBean2
```

### Generate image without legend

```
./run.sh blueprintFile.xml --no-legend
```
