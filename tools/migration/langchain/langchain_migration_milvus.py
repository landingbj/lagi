import argparse
import requests
import json
from langchain_community.vectorstores import Milvus

# python langchain_migration_milvus.py -cargs {"host": "localhost", "port": "19530"}  -url http://127.0.0.1:8080 -category lagi -l system

def main():
    parser = argparse.ArgumentParser(description="这是一个 Milvus 数据库迁移工具")
    parser.add_argument('--connection_args', '-cargs', help='数据库索引')
    parser.add_argument('--url', '-url', help='中间件地址')
    parser.add_argument('--category', '-category', help='category')
    parser.add_argument('--level', '-l', help='level')
    args = parser.parse_args()
    connection_args = json.loads(args.connection_args) if args.connection_args else None
    url = args.url
    category = args.category
    level = args.level
    migrate(connection_args, url, category, level)

def migrate(connection_args, url, category, level):
    vector_store = get_db(connection_args)
    batch_size = 100  # 每批处理的向量数量
    # 获取 Milvus 中的所有文档
    documents = vector_store.get()
    total_vectors = len(documents['ids'])
    data = []
    last_source = None
    print(total_vectors, batch_size)
    for start in range(0, total_vectors, batch_size):
        end = min(start + batch_size, total_vectors)
        for i in range(start, end):
            doc_id = documents['ids'][i]
            doc = documents['docs'][i]
            embedding = vector_store.embedding_function(doc.page_content)
            metadata = doc.metadata
            metadata = convert_metadata(metadata, category, level)
            print(f"文档内容: {doc.page_content}, 元数据: {metadata}, 向量: {embedding}")
            cur_source = metadata["source"]
            if not last_source or last_source == cur_source:
                data.append({"document": doc.page_content, "metadata": metadata})
            else:
                add_indexes(url, data, category)
                data = []
                data.append({"document": doc.page_content, "metadata": metadata})
            last_source = cur_source
    add_indexes(url, data, category)


def convert_metadata(meta, category, level):
    meta["category"] = category
    meta["level"] = level
    meta["filename"] = meta["source"]
    meta["filepath"] = meta["source"]
    return meta


def get_db(connection_args):
    return Milvus(
        embedding_function=None,
        collection_name="your_collection_name",  # 请根据实际情况修改
        connection_args=connection_args  # 请根据实际情况修改
    )


def add_indexes(url, data: list, category: str, link=True):
    url = url + "/v1/vector/upsert"
    headers = {
        'Content-Type': 'application/json'
    }
    payload = json.dumps({
        "category": category,
        "isContextLinked": link,
        "data": data
    })
    response = requests.request("POST", url, headers=headers, data=payload)
    if len(data) > 0:
        print(f'迁移文件:{data[0]["metadata"]["source"]} \t 结果：{response.text}')


if __name__ == '__main__':
    main()