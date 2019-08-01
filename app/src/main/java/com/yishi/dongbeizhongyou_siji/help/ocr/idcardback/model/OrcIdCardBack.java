/**
  * Copyright 2018 bejson.com 
  */
package com.megvii.dongbeizhongyou_siji.help.ocr.idcardback.model;

import com.yishi.dongbeizhongyou_siji.help.ocr.idcard.model.Legality;

/**
 * Auto-generated: 2018-06-09 9:31:48
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class OrcIdCardBack {

    private Legality legality;
    private String request_id;
    private int time_used;
    private String valid_date;
    private String issued_by;
    private String side;
    public void setLegality(Legality legality) {
         this.legality = legality;
     }
     public Legality getLegality() {
         return legality;
     }

    public void setRequest_id(String request_id) {
         this.request_id = request_id;
     }
     public String getRequest_id() {
         return request_id;
     }

    public void setTime_used(int time_used) {
         this.time_used = time_used;
     }
     public int getTime_used() {
         return time_used;
     }

    public void setValid_date(String valid_date) {
         this.valid_date = valid_date;
     }
     public String getValid_date() {
         return valid_date;
     }

    public void setIssued_by(String issued_by) {
         this.issued_by = issued_by;
     }
     public String getIssued_by() {
         return issued_by;
     }

    public void setSide(String side) {
         this.side = side;
     }
     public String getSide() {
         return side;
     }

}