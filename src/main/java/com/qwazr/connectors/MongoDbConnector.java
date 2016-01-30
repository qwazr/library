/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.qwazr.connectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.*;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.StringUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoDbConnector extends AbstractConnector {

	private MongoClient mongoClient = null;

	public static class MongoDbCredential {

		final public String username = null;

		final public String database = null;

		@JsonIgnore
		private String password = null;

		@JsonProperty("password")
		private void setPassword(String password) {
			this.password = password;
		}

	}

	public static class MongoServerAddress {
		final public String hostname = null;
		final public Integer port = null;
	}

	public List<MongoDbCredential> credentials;
	public List<MongoServerAddress> servers;
	public Integer port;

	@Override
	public void load(File data_directory) {
		List<ServerAddress> serverAddresses = new ArrayList();
		for (MongoServerAddress server : servers) {
			ServerAddress serverAddress = server.port == null ?
					new ServerAddress(server.hostname) :
					new ServerAddress(server.hostname, server.port);
			serverAddresses.add(serverAddress);
		}
		if (credentials == null || credentials.isEmpty()) {
			mongoClient = new MongoClient(serverAddresses);
		} else {
			List<MongoCredential> mongoCredentials = new ArrayList<MongoCredential>(credentials.size());
			for (MongoDbCredential credential : credentials)
				mongoCredentials.add(MongoCredential.createMongoCRCredential(credential.username, credential.database,
						credential.password.toCharArray()));
			mongoClient = new MongoClient(serverAddresses, mongoCredentials);
		}

	}

	@Override
	public void close() {
		if (mongoClient != null) {
			IOUtils.close(mongoClient);
			mongoClient = null;
		}
	}

	/**
	 * Return a Mongo DB instance
	 *
	 * @param databaseName the name of the database
	 * @return a MongoDatabase object
	 * @throws IOException if any I/O error occurs
	 */
	@JsonIgnore
	public MongoDatabase getDatabase(String databaseName) throws IOException {
		if (StringUtils.isEmpty(databaseName))
			throw new IOException("No database name.");
		return mongoClient.getDatabase(databaseName);
	}

	public void createCollection(String databaseName, String collectionName) throws IOException {
		if (StringUtils.isEmpty(collectionName))
			throw new IOException("No collection name.");
		getDatabase(databaseName).createCollection(collectionName);
	}

	/**
	 * Returns a DB collection instance
	 *
	 * @param databaseName   the name of the Database
	 * @param collectionName the name of the collection
	 * @return a MongoCollection object
	 * @throws IOException if any I/O error occurs
	 */
	@JsonIgnore
	public MongoCollectionDecorator getCollection(String databaseName, String collectionName) throws IOException {
		if (StringUtils.isEmpty(collectionName))
			throw new IOException("No collection name.");
		return new MongoCollectionDecorator(getDatabase(databaseName).getCollection(collectionName));
	}

	/**
	 * Build a BSON Document from a JSON string
	 *
	 * @param json the JSON string
	 * @return a Document or NULL if json is empty
	 */
	@JsonIgnore
	public Document getNewDocument(String json) {
		if (StringUtils.isEmpty(json))
			return null;
		return Document.parse(json);
	}

	/**
	 * Build a BSON Document from a MAP
	 *
	 * @param map a map
	 * @return a Document or NULL if the MAP is null
	 */
	@JsonIgnore
	public Document getNewDocument(Map<String, Object> map) {
		if (map == null)
			return null;
		return new Document(map);
	}

	/**
	 * Create a new UpdateOptions object
	 *
	 * @param upsert true if a new document should be inserted if there are no
	 *               matches to the query filter
	 * @return a new UpdateOptions object
	 */
	@JsonIgnore
	public UpdateOptions getNewUpdateOptions(boolean upsert) {
		UpdateOptions updateOptions = new UpdateOptions();
		updateOptions.upsert(upsert);
		return updateOptions;
	}

	@JsonIgnore
	public MongoBulk getNewBulk() {
		return new MongoBulk();
	}

	@JsonIgnore
	public BulkWriteOptions getNewBulkWriteOptions(boolean ordered) {
		return new BulkWriteOptions().ordered(ordered);
	}

	public static class MongoCollectionDecorator implements MongoCollection<Document> {

		private final MongoCollection<Document> collection;

		private MongoCollectionDecorator(MongoCollection<Document> collection) {
			this.collection = collection;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MongoNamespace getNamespace() {
			return collection.getNamespace();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<Document> getDocumentClass() {
			return collection.getDocumentClass();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public CodecRegistry getCodecRegistry() {
			return collection.getCodecRegistry();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ReadPreference getReadPreference() {
			return collection.getReadPreference();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public WriteConcern getWriteConcern() {
			return collection.getWriteConcern();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ReadConcern getReadConcern() {
			return collection.getReadConcern();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <NewTDocument> MongoCollection<NewTDocument> withDocumentClass(Class<NewTDocument> clazz) {
			return collection.withDocumentClass(clazz);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MongoCollection<Document> withCodecRegistry(CodecRegistry codecRegistry) {
			return collection.withCodecRegistry(codecRegistry);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MongoCollection<Document> withReadPreference(ReadPreference readPreference) {
			return collection.withReadPreference(readPreference);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MongoCollection<Document> withWriteConcern(WriteConcern writeConcern) {
			return collection.withWriteConcern(writeConcern);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MongoCollection<Document> withReadConcern(ReadConcern readConcern) {
			return collection.withReadConcern(readConcern);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long count() {
			return collection.count();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long count(Bson filter) {
			return collection.count(filter);
		}

		/**
		 * @param filter An object described as a Map
		 * @see MongoCollection#count(Bson)
		 */
		public long count(Map<String, Object> filter) {
			return collection.count(new Document(filter));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long count(Bson filter, CountOptions options) {
			return collection.count(filter, options);
		}

		/**
		 * @return a new CountOptions instance
		 * @see CountOptions
		 */
		public CountOptions getNewCountOption() {
			return new CountOptions();
		}

		/**
		 * @param filter  the query filter
		 * @param options the options describing the count
		 * @return the number of documents in the collection
		 * @see MongoCollection#count(Bson, CountOptions)
		 */
		public long count(Map<String, Object> filter, CountOptions options) {
			return collection.count(new Document(filter), options);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> DistinctIterable<TResult> distinct(String fieldName, Class<TResult> tResultClass) {
			return collection.distinct(fieldName, tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> DistinctIterable<TResult> distinct(String fieldName, Bson filter,
				Class<TResult> tResultClass) {
			return collection.distinct(fieldName, filter, tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public FindIterable<Document> find() {
			return collection.find();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> FindIterable<TResult> find(Class<TResult> tResultClass) {
			return collection.find(tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public FindIterable<Document> find(Bson filter) {
			return collection.find(filter);
		}

		/**
		 * @param filter the query filter
		 * @return the find iterable interface
		 * @see MongoCollection#find(Bson)
		 */
		public FindIterable<Document> find(Map<String, Object> filter) {
			return collection.find(new Document(filter));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> FindIterable<TResult> find(Bson filter, Class<TResult> tResultClass) {
			return collection.find(filter, tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public AggregateIterable<Document> aggregate(List<? extends Bson> pipeline) {
			return collection.aggregate(pipeline);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> AggregateIterable<TResult> aggregate(List<? extends Bson> pipeline,
				Class<TResult> tResultClass) {
			return collection.aggregate(pipeline, tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MapReduceIterable<Document> mapReduce(String mapFunction, String reduceFunction) {
			return collection.mapReduce(mapFunction, reduceFunction);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> MapReduceIterable<TResult> mapReduce(String mapFunction, String reduceFunction,
				Class<TResult> tResultClass) {
			return collection.mapReduce(mapFunction, reduceFunction, tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests) {
			return collection.bulkWrite(requests);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests,
				BulkWriteOptions options) {
			return collection.bulkWrite(requests, options);
		}

		/**
		 * @param requests the writes to execute
		 * @param ordered
		 * @return the result of the bulk write
		 * @see MongoCollection#bulkWrite(List, BulkWriteOptions)
		 */
		public BulkWriteResult bulkWrite(List<? extends WriteModel<? extends Document>> requests, boolean ordered) {
			return collection.bulkWrite(requests, new BulkWriteOptions().ordered(ordered));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertOne(Document document) {
			collection.insertOne(document);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertOne(Document document, InsertOneOptions insertOneOptions) {
			collection.insertOne(document, insertOneOptions);
		}

		/**
		 * @param document                 the document to insert
		 * @param bypassDocumentValidation
		 * @see MongoCollection#insertOne(Object, InsertOneOptions)
		 */
		public void insertOne(Map<String, Object> document, boolean bypassDocumentValidation) {
			collection.insertOne(new Document(document),
					new InsertOneOptions().bypassDocumentValidation(bypassDocumentValidation));
		}

		/**
		 * @param document the document to insert
		 * @see MongoCollection#insertOne(Object)
		 */
		public void insertOne(Map<String, Object> document) {
			collection.insertOne(new Document(document));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertMany(List<? extends Document> tDocuments) {
			collection.insertMany(tDocuments);
		}

		public List<Document> getNewDocumentList(ScriptObjectMirror documents) throws ScriptException {
			if (!documents.isArray())
				throw new ScriptException("An array is expected, not an object");
			final List<Document> list = new ArrayList<Document>();
			documents.forEach((s, o) -> list.add(new Document((Map<String, Object>) o)));
			return list;
		}

		/**
		 * @param documents the documents to insert
		 * @throws ScriptException
		 * @see MongoCollection#insertMany(List)
		 */
		public void insertMany(ScriptObjectMirror documents) throws ScriptException {
			collection.insertMany(getNewDocumentList(documents));
		}

		/**
		 * @return a new InsertManyOptions instance
		 * @see InsertManyOptions
		 */
		public InsertManyOptions getNewInsertManyOptions() {
			return new InsertManyOptions();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void insertMany(List<? extends Document> documents, InsertManyOptions options) {
			collection.insertMany(documents, options);
		}

		public void insertMany(ScriptObjectMirror documents, InsertManyOptions options) throws ScriptException {
			collection.insertMany(getNewDocumentList(documents), options);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public DeleteResult deleteOne(Bson filter) {
			return collection.deleteOne(filter);
		}

		public DeleteResult deleteOne(Map<String, Object> filter) {
			return collection.deleteOne(new Document(filter));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public DeleteResult deleteMany(Bson filter) {
			return collection.deleteMany(filter);
		}

		public DeleteResult deleteMany(Map<String, Object> filter) {
			return collection.deleteMany(new Document(filter));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UpdateResult replaceOne(Bson filter, Document replacement) {
			return collection.replaceOne(filter, replacement);
		}

		public UpdateResult replaceOne(Map<String, Object> filter, Map<String, Object> replacement) {
			return collection.replaceOne(new Document(filter), new Document(replacement));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UpdateResult replaceOne(Bson filter, Document replacement, UpdateOptions updateOptions) {
			return collection.replaceOne(filter, replacement, updateOptions);
		}

		public UpdateResult replaceOne(Map<String, Object> filter, Map<String, Object> replacement, boolean upsert) {
			return collection
					.replaceOne(new Document(filter), new Document(replacement), new UpdateOptions().upsert(upsert));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UpdateResult updateOne(Bson filter, Bson update) {
			return collection.updateOne(filter, update);
		}

		public UpdateResult updateOne(Map<String, Object> filter, Map<String, Object> update) {
			return collection.updateOne(new Document(filter), new Document(update));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UpdateResult updateOne(Bson filter, Bson update, UpdateOptions updateOptions) {
			return collection.updateOne(filter, update, updateOptions);
		}

		public UpdateResult updateOne(Map<String, Object> filter, Map<String, Object> update, boolean upsert) {
			return collection.updateOne(new Document(filter), new Document(update), new UpdateOptions().upsert(upsert));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UpdateResult updateMany(Bson filter, Bson update) {
			return collection.updateMany(filter, update);
		}

		public UpdateResult updateMany(Map<String, Object> filter, Map<String, Object> update) {
			return collection.updateMany(new Document(filter), new Document(update));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public UpdateResult updateMany(Bson filter, Bson update, UpdateOptions updateOptions) {
			return collection.updateMany(filter, update, updateOptions);
		}

		public UpdateResult updateMany(Map<String, Object> filter, Map<String, Object> update, boolean upsert) {
			return collection
					.updateMany(new Document(filter), new Document(update), new UpdateOptions().upsert(upsert));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Document findOneAndDelete(Bson filter) {
			return collection.findOneAndDelete(filter);
		}

		public Document findOneAndDelete(Map<String, Object> filter) {
			return collection.findOneAndDelete(new Document(filter));
		}

		public FindOneAndDeleteOptions getNewFindOneAndDeleteOptions() {
			return new FindOneAndDeleteOptions();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Document findOneAndDelete(Bson filter, FindOneAndDeleteOptions options) {
			return collection.findOneAndDelete(filter, options);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Document findOneAndReplace(Bson filter, Document replacement) {
			return collection.findOneAndReplace(filter, replacement);
		}

		public Document findOneAndReplace(Map<String, Object> filter, Map<String, Object> replacement) {
			return collection.findOneAndReplace(new Document(filter), new Document(replacement));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Document findOneAndReplace(Bson filter, Document replacement, FindOneAndReplaceOptions options) {
			return collection.findOneAndReplace(filter, replacement, options);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Document findOneAndUpdate(Bson filter, Bson update) {
			return collection.findOneAndUpdate(filter, update);
		}

		public Document findOneAndUpdate(Map<String, Object> filter, Map<String, Object> update) {
			return collection.findOneAndUpdate(new Document(filter), new Document(update));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Document findOneAndUpdate(Bson filter, Bson update, FindOneAndUpdateOptions options) {
			return collection.findOneAndUpdate(filter, update, options);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void drop() {
			collection.drop();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String createIndex(Bson keys) {
			return collection.createIndex(keys);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String createIndex(Bson keys, IndexOptions indexOptions) {
			return collection.createIndex(keys, indexOptions);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public List<String> createIndexes(List<IndexModel> indexes) {
			return collection.createIndexes(indexes);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ListIndexesIterable<Document> listIndexes() {
			return collection.listIndexes();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <TResult> ListIndexesIterable<TResult> listIndexes(Class<TResult> tResultClass) {
			return collection.listIndexes(tResultClass);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dropIndex(String indexName) {
			collection.dropIndex(indexName);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dropIndex(Bson keys) {
			collection.dropIndex(keys);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dropIndexes() {
			collection.dropIndexes();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void renameCollection(MongoNamespace newCollectionNamespace) {
			collection.renameCollection(newCollectionNamespace);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void renameCollection(MongoNamespace newCollectionNamespace,
				RenameCollectionOptions renameCollectionOptions) {
			collection.renameCollection(newCollectionNamespace, renameCollectionOptions);
		}
	}

	public static class MongoBulk extends ArrayList<WriteModel<Document>> {

		public MongoBulk addDeleteMany(Map<String, Object> filter) {
			add(new DeleteManyModel<>(new Document(filter)));
			return this;
		}

		public MongoBulk addDeleteOne(Map<String, Object> filter) {
			add(new DeleteOneModel<>(new Document(filter)));
			return this;
		}

		public MongoBulk addInsertOne(Map<String, Object> document) {
			add(new InsertOneModel(new Document(document)));
			return this;
		}

		public MongoBulk addReplaceOne(Map<String, Object> filter, Map<String, Object> replacement) {
			add(new ReplaceOneModel(new Document(filter), new Document(replacement)));
			return this;
		}

		public MongoBulk addReplaceOne(Map<String, Object> filter, Map<String, Object> replacement, boolean upsert) {
			add(new ReplaceOneModel(new Document(filter), new Document(replacement),
					new UpdateOptions().upsert(upsert)));
			return this;
		}

		public MongoBulk addUpdateOne(Map<String, Object> filter, Map<String, Object> replacement) {
			add(new UpdateOneModel(new Document(filter), new Document(replacement)));
			return this;
		}

		public MongoBulk addUpdateOne(Map<String, Object> filter, Map<String, Object> replacement, boolean upsert) {
			add(new UpdateOneModel(new Document(filter), new Document(replacement),
					new UpdateOptions().upsert(upsert)));
			return this;
		}

		public MongoBulk addUpdateMany(Map<String, Object> filter, Map<String, Object> replacement) {
			add(new UpdateManyModel(new Document(filter), new Document(replacement)));
			return this;
		}

		public MongoBulk addUpdateMany(Map<String, Object> filter, Map<String, Object> replacement, boolean upsert) {
			add(new UpdateManyModel(new Document(filter), new Document(replacement),
					new UpdateOptions().upsert(upsert)));
			return this;
		}
	}
}
