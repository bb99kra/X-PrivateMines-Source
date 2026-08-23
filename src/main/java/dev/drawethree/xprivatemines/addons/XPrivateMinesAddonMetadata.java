package dev.drawethree.xprivatemines.addons;

import lombok.Generated;

public final class XPrivateMinesAddonMetadata {
   private final String name;
   private final String description;
   private final String version;
   private final String author;
   private final String minRequiredVersion;

   public XPrivateMinesAddonMetadata(String name, String description, String version, String author, String minRequiredVersion) {
      this.name = name != null ? name : "Unknown";
      this.description = description != null ? description : "No description";
      this.version = version != null ? version : "Unknown";
      this.author = author != null ? author : "Unknown";
      this.minRequiredVersion = minRequiredVersion;
   }

   public XPrivateMinesAddonMetadata() {
      this.name = "Unknown";
      this.description = "No description";
      this.version = "Unknown";
      this.author = "Unknown";
      this.minRequiredVersion = null;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDescription() {
      return this.description;
   }

   @Generated
   public String getVersion() {
      return this.version;
   }

   @Generated
   public String getAuthor() {
      return this.author;
   }

   @Generated
   public String getMinRequiredVersion() {
      return this.minRequiredVersion;
   }
}
