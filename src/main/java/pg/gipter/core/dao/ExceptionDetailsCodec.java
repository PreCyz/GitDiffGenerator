package pg.gipter.core.dao;

import com.mongodb.MongoClientSettings;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import pg.gipter.statistics.ExceptionDetails;

public class ExceptionDetailsCodec implements CollectibleCodec<ExceptionDetails> {

    private final CodecRegistry registry;
    private final Codec<Document> documentCodec;
    private final ExceptionDetailsConverter converter;

    public ExceptionDetailsCodec() {
        this.registry = MongoClientSettings.getDefaultCodecRegistry();
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new ExceptionDetailsConverter();
    }

    public ExceptionDetailsCodec(Codec<Document> codec) {
        this.documentCodec = codec;
        this.registry = MongoClientSettings.getDefaultCodecRegistry();
        this.converter = new ExceptionDetailsConverter();
    }

    public ExceptionDetailsCodec(CodecRegistry registry) {
        this.registry = registry;
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new ExceptionDetailsConverter();
    }

    @Override
    public void encode(BsonWriter writer, ExceptionDetails exceptionDetails, EncoderContext encoderContext) {
        Document document = this.converter.convert(exceptionDetails);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<ExceptionDetails> getEncoderClass() {
        return ExceptionDetails.class;
    }

    @Override
    public ExceptionDetails decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return this.converter.convert(document);
    }

    @Override
    public ExceptionDetails generateIdIfAbsentFromDocument(ExceptionDetails exceptionDetails) {
        return exceptionDetails;
    }

    @Override
    public boolean documentHasId(ExceptionDetails exceptionDetails) {
        return true;
    }

    @Override
    public BsonValue getDocumentId(ExceptionDetails exceptionDetails) {
        if (!documentHasId(exceptionDetails)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        return new BsonString(new ObjectId().toHexString());
    }
}
