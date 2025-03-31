
import argparse
import requests
import json

from langchain_community.vectorstores import FAISS
# from langchain.embeddings import *


# python langchain_migration.py -p ./data -v Chroma -url http://127.0.0.1:8080 -category lagi -l system

def main():
    parser = argparse.ArgumentParser(description="这是一个数据库迁移工具")
    parser.add_argument('--index', '-index', help='数据库索引')
    parser.add_argument('--url', '-url', help='中间件地址')
    parser.add_argument('--category', '-category', help='category')
    parser.add_argument('--level', '-l', help='level')
    args = parser.parse_args()
    index = args.index
    url = args.url
    category = args.category
    level = args.level
    migrate(index, url, category, level)

def migrate(index, url, category, level):
    vector_store = get_db(index)
    batch_size = 100  # 每批处理的向量数量
    total_vectors = vector_store.index.ntotal
    data = []
    last_source = None
    print(total_vectors, batch_size)
    for start in range(0, total_vectors, batch_size):
        end = min(start + batch_size, total_vectors)
        batch_embeddings = vector_store.index.reconstruct_n(start, end - start)
        for i in range(len(batch_embeddings)):
            doc_id = vector_store.index_to_docstore_id[start + i]
            doc = vector_store.docstore._dict[doc_id]
            embedding = batch_embeddings[i]
            metadata = doc.metadata
            metadata = convert_metadata(metadata, category, level)
            print(f"文档内容: {doc.page_content}, 元数据: {metadata}, 向量: {embedding}")
            cur_source = metadata["source"]
            if not last_source or last_source == cur_source:
                data.append({"document": doc.page_content,"metadata": metadata})
            else:
                add_indexes(url, data, category)
                data = []
                data.append({"document": doc.page_content,"metadata": metadata})
            last_source = cur_source
        # 可以在这里释放 batch_embeddings 占用的内存
        del batch_embeddings
    add_indexes(url, data, category)


def convert_metadata(meta, category, level):
    meta["category"] = category
    meta["level"] = level
    meta["filename"] = meta["source"]
    meta["filepath"] = meta["source"]
    return meta

def get_db(index):
    return FAISS.load_local(
        embeddings=None,
        folder_path = index,
        allow_dangerous_deserialization=True
    )


def add_indexes(url, data: list ,category: str, link = True):
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