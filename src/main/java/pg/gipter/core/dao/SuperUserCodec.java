package pg.gipter.core.dao;

import com.mongodb.MongoClient;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import pg.gipter.users.SuperUser;

public class SuperUserCodec implements CollectibleCodec<SuperUser> {

    private final CodecRegistry registry;
    private final Codec<Document> documentCodec;
    private final SuperUserConverter converter;

    public SuperUserCodec() {
        this.registry = MongoClient.getDefaultCodecRegistry();
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new SuperUserConverter();
    }

    public SuperUserCodec(Codec<Document> codec) {
        this.documentCodec = codec;
        this.registry = MongoClient.getDefaultCodecRegistry();
        this.converter = new SuperUserConverter();
    }

    public SuperUserCodec(CodecRegistry registry) {
        this.registry = registry;
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new SuperUserConverter();
    }

    @Override
    public void encode(BsonWriter writer, SuperUser superUser, EncoderContext encoderContext) {
        Document document = this.converter.convert(superUser);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<SuperUser> getEncoderClass() {
        return SuperUser.class;
    }

    @Override
    public SuperUser decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return this.converter.convert(document);
    }

    @Override
    public SuperUser generateIdIfAbsentFromDocument(SuperUser superUser) {
        if (!documentHasId(superUser)) {
            superUser.setId(new ObjectId());
        }

        return superUser;
    }

    @Override
    public boolean documentHasId(SuperUser superUser) {
        return (superUser.getId() != null);
    }

    @Override
    public BsonValue getDocumentId(SuperUser superUser) {
        if (!documentHasId(superUser)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        return new BsonString(superUser.getId().toHexString());
    }

}
