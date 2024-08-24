package pg.gipter.core.dao;

import com.mongodb.MongoClientSettings;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import pg.gipter.core.config.GeneralSettings;

public class GeneralSettingsCodec implements CollectibleCodec<GeneralSettings> {

    private final CodecRegistry registry;
    private final Codec<Document> documentCodec;
    private final GeneralSettingsConverter converter;

    public GeneralSettingsCodec() {
        this.registry = MongoClientSettings.getDefaultCodecRegistry();
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new GeneralSettingsConverter();
    }

    public GeneralSettingsCodec(Codec<Document> codec) {
        this.documentCodec = codec;
        this.registry = MongoClientSettings.getDefaultCodecRegistry();
        this.converter = new GeneralSettingsConverter();
    }

    public GeneralSettingsCodec(CodecRegistry registry) {
        this.registry = registry;
        this.documentCodec = this.registry.get(Document.class);
        this.converter = new GeneralSettingsConverter();
    }

    @Override
    public void encode(BsonWriter writer, GeneralSettings generalSettings, EncoderContext encoderContext) {
        Document document = this.converter.convert(generalSettings);
        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<GeneralSettings> getEncoderClass() {
        return GeneralSettings.class;
    }

    @Override
    public GeneralSettings decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return this.converter.convert(document);
    }

    @Override
    public GeneralSettings generateIdIfAbsentFromDocument(GeneralSettings generalSettings) {
        if (!documentHasId(generalSettings)) {
            generalSettings.setId(new ObjectId());
        }

        return generalSettings;
    }

    @Override
    public boolean documentHasId(GeneralSettings generalSettings) {
        return (generalSettings.getId() != null);
    }

    @Override
    public BsonValue getDocumentId(GeneralSettings generalSettings) {
        if (!documentHasId(generalSettings)) {
            throw new IllegalStateException("The document does not contain an _id");
        }

        return new BsonString(generalSettings.getId().toHexString());
    }

}
