# kotlin:kmp

<!--region chart-->
![chart](chart-d2.svg)

![chart](chart-graphviz.svg)

![legend](../../atlas/legend-graphviz.svg)

```mermaid
graph TD
  _java[":java"]
  _kotlin_jvm[":kotlin:jvm"]
  _kotlin_kmp[":kotlin:kmp"]
  _other[":other"]
  _java --> _other
  _kotlin_kmp --> _java
  _kotlin_kmp --> _kotlin_jvm
```
<!--endregion-->