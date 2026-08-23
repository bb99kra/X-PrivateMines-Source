package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayServerEffect extends PacketWrapper<WrapperPlayServerEffect> {
   private int type;
   private Vector3i position;
   private int data;
   private boolean globalEvent;

   public WrapperPlayServerEffect(PacketSendEvent type) {
      super(type);
   }

   public WrapperPlayServerEffect(int type, Vector3i position, int data, boolean globalEvent) {
      super(Server.EFFECT);
      this.type = type;
      this.position = position;
      this.data = data;
      this.globalEvent = globalEvent;
   }

   public void read() {
      this.type = this.readInt();
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
         this.position = this.readBlockPosition();
      } else {
         this.position = new Vector3i(this.readInt(), this.readByte() & 255, this.readInt());
      }

      this.data = this.readInt();
      this.globalEvent = this.readBoolean();
   }

   public void write() {
      this.writeInt(this.type);
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
         this.writeBlockPosition(this.position);
      } else {
         this.writeInt(this.position.x);
         this.writeByte(this.position.y & 0xFF);
         this.writeInt(this.position.z);
      }

      this.writeInt(this.data);
      this.writeBoolean(this.globalEvent);
   }

   public void copy(WrapperPlayServerEffect wrapper) {
      this.type = wrapper.type;
      this.position = wrapper.position;
      this.data = wrapper.data;
      this.globalEvent = wrapper.globalEvent;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public Vector3i getPosition() {
      return this.position;
   }

   public void setPosition(Vector3i position) {
      this.position = position;
   }

   public int getData() {
      return this.data;
   }

   public void setData(int data) {
      this.data = data;
   }

   public boolean isGlobalEvent() {
      return this.globalEvent;
   }

   public void setGlobalEvent(boolean globalEvent) {
      this.globalEvent = globalEvent;
   }
}
