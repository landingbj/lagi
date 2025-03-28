
import argparse
import requests
import json
import inspect

from langchain_community.vectorstores import Chroma
# from langchain.embeddings import *


# python langchain_migration.py -p ./data -v Chroma -url http://127.0.0.1:8080 -category lagi -l system

def main():
    parser = argparse.ArgumentParser(description="这是一个数据库迁移工具")
    parser.add_argument('--vector_args', '-vargs', help='向量数据库参数')
    # parser.add_argument('--vector_name', '-v', help='源向量数据库类型')
    parser.add_argument('--persist_path', '-p', help='源持久化地址')
    parser.add_argument('--collection', '-c', help='源数据数据集')
    # parser.add_argument('--embedding_name', '-ename', help='源embedding')
    # parser.add_argument('--embedding_key', '-ekey', help='源embedding key')
    parser.add_argument('--url', '-url', help='中间件地址')
    parser.add_argument('--category', '-category', help='category')
    parser.add_argument('--level', '-l', help='level')
    args = parser.parse_args()
    persist_path = args.persist_path
    # vector_name = args.vector_name
    collection = args.collection
    # embedding_name = args.embedding_name
    # embedding_key = args.embedding_key
    url = args.url
    category = args.category
    level = args.level
    migrate(persist_path, collection, url, category, level)

def migrate(persist_path, collection, url, category, level):
    db_args = {
    }
    if persist_path:
        db_args["persist_directory"] = persist_path
    if collection:
        db_args["collection_name"] = collection
    source_vector_db =  Chroma(**db_args)
    # source_vector_db = get_db(persist_path , collection)
    documents = source_vector_db.get(include=['documents', 'embeddings', 'metadatas'])
    add_indexes(url, category, level,documents)



def is_imported_class_name(class_name_str):
    # 获取当前全局命名空间
    global_namespace = globals()

    # 遍历全局命名空间中的所有对象
    for _, obj in global_namespace.items():
        # 检查对象是否为类
        if inspect.isclass(obj):
            # 检查类名是否与输入的字符串匹配
            if obj.__name__ == class_name_str:
                return obj
    return None

def create_object(class_name_str, *args, **kwargs):
    class_obj = is_imported_class_name(class_name_str)
    if class_obj:
        try:
            return class_obj(*args, **kwargs)
        except Exception as e:
            print(f"创建对象时出错: {e}")
    else:
        print(f"未找到名为 {class_name_str} 的类。")
    return None


def convert_metadata(meta, category, level):
    meta["category"] = category
    meta["level"] = level
    meta["filename"] = meta["source"]
    meta["filepath"] = meta["source"]
    return meta

def get_db(path, db_name , collection):
    db_args = {
    }
    if path:
        db_args["persist_directory"] = path
    if collection:
        db_args["collection_name"] = collection
    db = None
    db = create_object(db_name, **db_args)
    return db



def add_indexes(url, category: str, level: str,documents):
    url = url + "/v1/vector/upsert"
    data = []
    last_source = None
    headers = {
        'Content-Type': 'application/json'
    }
    for doc, meta, emb in zip(documents['documents'], documents.get('metadatas', []), documents['embeddings']):
        meta = convert_metadata(meta, category, level)
        cur_source =  meta["source"]
        if last_source == cur_source or (not last_source):
            data.append({
                "document": doc,
                "metadata": meta
            })
        else:
            payload = json.dumps({
                "category": category,
                "isContextLinked": True,
                "data": data
            })
            response = requests.request("POST", url, headers=headers, data=payload)
            print(f"迁移文件:{last_source} \t 结果：{response.text}" )
            data = []
            data.append({
                "document": doc,
                "metadata": meta
            })
        last_source = cur_source
    payload = json.dumps({
                "category": category,
                "isContextLinked": True,
                "data": data
            })
    response = requests.request("POST", url, headers=headers, data=payload)
    print(f"迁移文件:{cur_source} \t 结果：{response.text}" )
    
    



if __name__ == '__main__':
    main()