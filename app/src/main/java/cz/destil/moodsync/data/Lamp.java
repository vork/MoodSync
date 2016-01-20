package cz.destil.moodsync.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by benediktboss on 20/01/16.
 */
public class Lamp extends RealmObject {
  @PrimaryKey
  private String id;
  private int groupAssigned;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getGroupAssigned() {
    return groupAssigned;
  }

  public void setGroupAssigned(int groupAssigned) {
    this.groupAssigned = groupAssigned;
  }
}
