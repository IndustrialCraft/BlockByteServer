package com.github.industrialcraft.blockbyteserver.world;

import com.github.industrialcraft.blockbyteserver.net.MessageS2C;
import com.github.industrialcraft.blockbyteserver.util.AABB;
import com.github.industrialcraft.blockbyteserver.util.Position;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    public final int clientId;
    public final UUID id;
    protected Position position;
    protected float rotation;
    protected Chunk chunk;
    private boolean removed;
    public Entity(Position position, World world) {
        this.clientId = ID_GENERATOR.incrementAndGet();
        this.id = UUID.randomUUID();
        this.position = position;
        this.chunk = world.getChunk(position.toBlockPos().toChunkPos());
        if(this.chunk == null)
            throw new IllegalStateException("Attempting to spawn entity to unloaded chunk");
        this.chunk.addEntity(this);
        this.removed = false;
        this.rotation = 0f;
    }
    public void tick(){}
    public AABB getBoundingBox(){
        return null;
    }
    public void teleport(Position position, World world){
        Chunk newChunk = world.getChunk(position.toBlockPos().toChunkPos());
        if(newChunk == null)
            throw new IllegalArgumentException("Attempting to teleport entity to unloaded chunk");
        this.position = position;
        if(this.chunk != newChunk){
            this.chunk.transferEntity(this, newChunk);
            this.chunk = newChunk;
        } else {
            this.chunk.announceToViewersExcept(new MessageS2C.MoveEntity(clientId, position.x(), position.y(), position.z(), rotation), null);
        }
    }
    public void teleport(Position position){
        teleport(position, chunk.parent);
    }
    public Position getPosition() {
        return position;
    }
    public Chunk getChunk() {
        return chunk;
    }
    public boolean isRemoved() {
        return removed;
    }
    public void remove(){
        this.removed = true;
    }
    public abstract int getClientType();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(id, entity.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
