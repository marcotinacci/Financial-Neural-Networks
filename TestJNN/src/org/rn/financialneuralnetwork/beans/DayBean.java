package org.rn.financialneuralnetwork.beans;

import java.util.Date;

/**
 *
 * @author Marco Tinacci
 */
public class DayBean {

   private Date date;
   private float open, high, low, close, adjClose;
   private long volume;

   public DayBean() {
   }

   public DayBean(Date date, float open, float high, float low, float close,
           float adjClose, long volume) {
      this.date = date;
      this.open = open;
      this.high = high;
      this.low = low;
      this.close = close;
      this.adjClose = adjClose;
      this.volume = volume;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DayBean d = new DayBean((Date)date.clone(), open, high, low, close,
              adjClose, volume);
      return d;
   }

   @Override
   @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
   public boolean equals(Object o) {
      DayBean d = (DayBean)o;
      return 
         date.equals(d.date) &&
         open == d.open &&
         high == d.high &&
         low == d.low &&
         close == d.close &&
         adjClose == d.adjClose &&
         volume == d.volume;
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 23 * hash + (this.date != null ? this.date.hashCode() : 0);
      hash = 23 * hash + Float.floatToIntBits(this.open);
      hash = 23 * hash + Float.floatToIntBits(this.high);
      hash = 23 * hash + Float.floatToIntBits(this.low);
      hash = 23 * hash + Float.floatToIntBits(this.close);
      hash = 23 * hash + Float.floatToIntBits(this.adjClose);
      hash = 23 * hash + (int)this.volume;
      return hash;
   }

   @Override
   public String toString() {
      return "[ " + date.getTime() + ", " +
              Float.toString(open) + ", " +
              Float.toString(high) + ", " +
              Float.toString(low) + ", " +
              Float.toString(close) + ", " +
              Float.toString(adjClose) + ", " +
              Long.toString(volume) + " ]";
   }
   
   /**
    * @return the date
    */
   public Date getDate(){
      return date;
   }

   /**
    * @param date the date to set
    */
   public void setDate(Date date) {
      this.date = date;
   }

   /**
    * @return the open
    */
   public float getOpen() {
      return open;
   }

   /**
    * @param open the open to set
    */
   public void setOpen(float open) {
      this.open = open;
   }

   /**
    * @return the high
    */
   public float getHigh() {
      return high;
   }

   /**
    * @param high the high to set
    */
   public void setHigh(float high) {
      this.high = high;
   }

   /**
    * @return the low
    */
   public float getLow() {
      return low;
   }

   /**
    * @param low the low to set
    */
   public void setLow(float low) {
      this.low = low;
   }

   /**
    * @return the close
    */
   public float getClose() {
      return close;
   }

   /**
    * @param close the close to set
    */
   public void setClose(float close) {
      this.close = close;
   }

   /**
    * @return the adjClose
    */
   public float getAdjClose() {
      return adjClose;
   }

   /**
    * @param adjClose the adjClose to set
    */
   public void setAdjClose(float adjClose) {
      this.adjClose = adjClose;
   }

   /**
    * @return the volume
    */
   public long getVolume() {
      return volume;
   }

   /**
    * @param volume the volume to set
    */
   public void setVolume(long volume) {
      this.volume = volume;
   }



}
