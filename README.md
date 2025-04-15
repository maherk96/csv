```sql
@JsonCreator
public QAPFeature(
    @JsonProperty("featureName") String featureName,
    @JsonProperty("featureTitle") String featureTitle,
    @JsonProperty("featureDescription") String featureDescription) {
    this.featureName = featureName;
    this.featureTitle = featureTitle;
    this.featureDescription = featureDescription;
    this.scenarios = new ArrayList<>();
}

```
