package VASSAL.configure.configuretree;

public class SearchParameters {

  /** Current search string */
  private String searchString;

  /** True if case-sensitive */
  private boolean matchCase;

  /** True if match configurable names */
  private boolean matchNames;

  /** True if match class names */
  private boolean matchTypes;

  public SearchParameters() {
    searchString = "";
    matchCase = false;
    matchTypes = true;
    matchNames = true;
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  public boolean isMatchCase() {
    return matchCase;
  }

  public void setMatchCase(boolean matchCase) {
    this.matchCase = matchCase;
  }

  public boolean isMatchNames() {
    return matchNames;
  }

  public void setMatchNames(boolean matchNames) {
    this.matchNames = matchNames;
  }

  public boolean isMatchTypes() {
    return matchTypes;
  }

  public void setMatchTypes(boolean matchTypes) {
    this.matchTypes = matchTypes;
  }
}
