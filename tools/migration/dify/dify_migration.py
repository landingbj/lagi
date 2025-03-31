import requests
import json
import os
import uuid
import argparse

# python dify_migration.py --category unicom --level system --tomcat_url http://127.0.0.1:8081 --tomcat_path "D:\\workEnv\\server\\tomcat1" --dify_url http://localhost --dify_api_key dataset-G7hrOHrq2NgT2CJzMBPr3RcE

def get_arguments():
    parser = argparse.ArgumentParser(description='Dify Migration Script')
    parser.add_argument('--category', required=True, help='Category for the data')
    parser.add_argument('--level', required=True, help='Level of the data')
    parser.add_argument('--tomcat_url', required=True, help='Tomcat server URL')
    parser.add_argument('--tomcat_path', required=True, help='Tomcat server path')
    parser.add_argument('--dify_url', required=True, help='Dify server URL')
    parser.add_argument('--dify_api_key', required=True, help='Dify API key')
    return parser.parse_args()

args = get_arguments()

category = args.category
level = args.level
tomcat_url = args.tomcat_url
tomcat_path = args.tomcat_path
dify_url = args.dify_url
dify_api_key = args.dify_api_key

tomcat_upload_path = os.path.join(tomcat_path, "webapps/ROOT/upload")

headers = {
    "Authorization": f"Bearer {dify_api_key}"
}

def get_all_knowledge_bases():
    """获取所有知识库信息"""
    tomcat_url = f"{dify_url}/v1/datasets"
    page = 1
    knowledge_bases = []

    while True:
        response = requests.get(f"{tomcat_url}?page={page}&limit=20", headers=headers)
        if response.status_code != 200:
            print(f"获取知识库列表失败，状态码：{response.status_code}")
            break

        data = response.json()
        knowledge_bases.extend(data['data'])

        if not data['has_more']:
            break
        page += 1

    return knowledge_bases

def get_documents_for_knowledge_base(dataset_id):
    """获取知识库下的所有文档"""
    tomcat_url = f"{dify_url}/v1/datasets/{dataset_id}/documents"
    page = 1
    documents = []

    while True:
        response = requests.get(f"{tomcat_url}?page={page}&limit=20", headers=headers)
        if response.status_code != 200:
            print(f"获取文档列表失败，状态码：{response.status_code}")
            break

        data = response.json()
        documents.extend(data['data'])

        if not data['has_more']:
            break
        page += 1

    return documents

def get_segments_for_document(dataset_id, document_id):
    """获取文档的所有分段"""
    tomcat_url = f"{dify_url}/v1/datasets/{dataset_id}/documents/{document_id}/segments"
    page = 1
    segments = []

    while True:
        response = requests.get(f"{tomcat_url}?page={page}&limit=20", headers=headers)
        if response.status_code != 200:
            print(f"获取分段列表失败，状态码：{response.status_code}")
            break

        data = response.json()
        segments.extend(data['data'])

        if not data['has_more']:
            break
        page += 1
    return segments


def main():
    # 获取所有知识库
    knowledge_bases = get_all_knowledge_bases()
    for kb in knowledge_bases:
        dataset_id = kb['id']
        print(f"处理知识库：{dataset_id}")
        # 获取知识库下的所有文档
        documents = get_documents_for_knowledge_base(dataset_id)
        for doc in documents:
            document_id = doc['id']
            name = doc['name']
            print(f"  处理文档：{document_id}")
            # 获取文档的所有分段
            segments = get_segments_for_document(dataset_id, document_id)
            download_url = get_uploadfile(dataset_id, document_id)
            uuid_filename =  get_file_suffix_and_concat_uuid(name)
            download_file(download_url, os.path.join(tomcat_upload_path, uuid_filename))
            data = convert2data(segments, category, level, name, uuid_filename)
            add_indexes(tomcat_url, data, category)


def get_file_suffix_and_concat_uuid(filename):
    # 获取文件后缀
    _, file_extension = os.path.splitext(filename)
    # 生成 UUID
    unique_id = uuid.uuid4()
    # 拼接 UUID 和文件后缀
    result = str(unique_id) + file_extension
    return result


def get_uploadfile(dataset_id, document_id):
    """获取上传文档"""
    tomcat_url = f"{dify_url}/v1/datasets/{dataset_id}/documents/{document_id}/upload-file"

    response = requests.get(tomcat_url, headers=headers)
    if response.status_code != 200:
        print(f"获取分段列表失败，状态码：{response.status_code}")
        return None
    data = response.json()
    return f'{dify_url}/{data["download_url"]}'


def download_file(url, save_path):
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()

        with open(save_path, 'wb') as file:
            for chunk in response.iter_content(chunk_size=8192):
                if chunk:
                    file.write(chunk)
        print(f"文件下载成功，保存路径为: {save_path}")
    except requests.exceptions.HTTPError as http_err:
        print(f"HTTP 错误发生: {http_err}")
    except requests.exceptions.RequestException as req_err:
        print(f"请求错误发生: {req_err}")
    except Exception as err:
        print(f"发生未知错误: {err}")


def convert2data(segments, category, level, filename, filepath):
    data = []
    for seg in segments:
        meta = {}
        meta["category"] = category
        meta["level"] = level
        meta["filename"] = filename
        meta["filepath"] = filepath
        data.append({"document": seg["content"], "metadata": meta})
    return data

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
        print(f'迁移文件:{data[0]["metadata"]["filename"]} \t 结果：{response.text}')


if __name__ == "__main__":
    main()