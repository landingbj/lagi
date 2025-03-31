import argparse
import requests
import json
import pinecone


def main():
    parser = argparse.ArgumentParser(description="这是一个 Pinecone 数据库迁移工具")
    parser.add_argument('--index', '-index', help='Pinecone 数据库索引')
    parser.add_argument('--api_key', '-key', help='Pinecone apikey')
    parser.add_argument('--env', '-env', help='Pinecone env')
    parser.add_argument('--url', '-url', help='目标中间件地址')
    parser.add_argument('--category', '-category', help='category')
    parser.add_argument('--level', '-l', help='level')
    args = parser.parse_args()
    index = args.index
    api_key = args.api_key
    env = args.env
    url = args.url
    category = args.category
    level = args.level
    migrate(api_key, env, index, url, category, level)

def migrate(api_key, env, index, url, category, level):
    pinecone.init(
        api_key=api_key,
        environment=env
    )
    pinecone_index = pinecone.Index(index)
    batch_size = 100  # 每批处理的向量数量
    data = []
    last_source = None
    # 获取向量总数
    stats = pinecone_index.describe_index_stats()
    total_vectors = stats['total_vector_count']
    print(total_vectors, batch_size)
    for start in range(0, total_vectors, batch_size):
        end = min(start + batch_size, total_vectors)
        # 从 Pinecone 中获取向量
        results = pinecone_index.fetch(ids=[str(i) for i in range(start, end)])
        for id, vector_info in results['vectors'].items():
            embedding = vector_info['values']
            metadata = vector_info['metadata']
            metadata = convert_metadata(metadata, category, level)
            document = metadata.get('document', '')
            print(f"文档内容: {document}, 元数据: {metadata}, 向量: {embedding}")
            cur_source = metadata["source"]
            if not last_source or last_source == cur_source:
                data.append({"document": document, "metadata": metadata})
            else:
                add_indexes(url, data, category)
                data = []
                data.append({"document": document, "metadata": metadata})
            last_source = cur_source
    add_indexes(url, data, category)

def convert_metadata(meta, category, level):
    meta["category"] = category
    meta["level"] = level
    meta["filename"] = meta["source"]
    meta["filepath"] = meta["source"]
    return meta

def add_indexes(url, data: list, category: str, link = True):
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