package pg.gipter.core.dao;

import com.mongodb.MongoClientSettings;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import pg.gipter.core.model.CipherDetails;

public class CipherDetailsCodec implements CollectibleCodec<CipherDetails> {

    private final CodecRegistry registry;
    private final Codec<Document> documentCodec;
    private final CipherDetailsConverter converter;

    public CipherDetailsCodec() {
        this.registry = MongoClientSettings.getDefaultCodecRegistry();
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new CipherDetailsConverter();
    }

    public CipherDetailsCodec(Codec<Document> codec) {
        this.documentCodec = codec;
        this.registry = MongoClientSettings.getDefaultCodecRegistry();
        this.converter = new CipherDetailsConverter();
    }

    public CipherDetailsCodec(CodecRegistry registry) {
        this.registry = registry;
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new CipherDetailsConverter();
    }
    @Override
    public CipherDetails generateIdIfAbsentFromDocument(CipherDetails cipherDetails) {
        if (!documentHasId(cipherDetails)) {
            cipherDetails.setId(new ObjectId());
        }
        return cipherDetails;
    }

    @Override
    public boolean documentHasId(CipherDetails cipherDetails) {
        return cipherDetails.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(CipherDetails cipherDetails) {
        if (!documentHasId(cipherDetails)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        return new BsonString(cipherDetails.getId().toHexString());
    }

    @Override
    public CipherDetails decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return this.converter.convert(document);
    }

    @Override
    public void encode(BsonWriter writer, CipherDetails cipherDetails, EncoderContext encoderContext) {
        Document document = this.converter.convert(cipherDetails);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<CipherDetails> getEncoderClass() {
        return CipherDetails.class;
    }
}
