import sqlite3

# 原始数据库连接
source_db_path = 'saas.db'
source_conn = sqlite3.connect(source_db_path)
source_cursor = source_conn.cursor()


tomcat_path = 'your_tomcat_path'

# 新数据库连接
target_db_path = f'{tomcat_path}/bin/saas.db'
target_conn = sqlite3.connect(target_db_path)
target_cursor = target_conn.cursor()

# 创建目标数据库中的表
target_cursor.execute('''
CREATE TABLE IF NOT EXISTS tree_diversify (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    text NOT NULL UNIQUE
);
''')

target_cursor.execute('''
CREATE TABLE IF NOT EXISTS tree_diversify_relations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    parent_id INT NOT NULL,
    child_id INT NOT NULL,
    hitCount INT NOT NULL DEFAULT 0,
    UNIQUE (parent_id, child_id)
);
''')

# 读取原始数据库中的 tree_diversify 表数据并插入到目标数据库
source_cursor.execute('SELECT id, text FROM tree_diversify ORDER BY id')
tree_diversify_data = source_cursor.fetchall()
total  = len(tree_diversify_data)
print(f'导入数据总条数 {total}')
# 创建一个映射，用于将原始数据库中的id映射到目标数据库中的id
id_mapping = {}

print('开始导入节点...')
for row in tree_diversify_data:
    original_id, text = row
    try:
        target_cursor.execute('INSERT INTO tree_diversify (text) VALUES (?)', (text,))
        new_id = target_cursor.lastrowid
    except Exception:
        target_cursor.execute('SELECT id, text FROM tree_diversify WHERE text = ?  ORDER BY id limit 1', (text,))
        data = target_cursor.fetchone()
        new_id = data[0];
    id_mapping[original_id] = new_id
print('节点导入完成...')

# 读取原始数据库中的 tree_diversify_relations 表数据并插入到目标数据库
source_cursor.execute('SELECT parent_id, child_id, hitCount FROM tree_diversify_relations ORDER BY parent_id')
tree_diversify_relations_data = source_cursor.fetchall()

print('开始导入关系...')
for row in tree_diversify_relations_data:
    original_parent_id, original_child_id, hitCount = row
    new_parent_id = id_mapping.get(original_parent_id)
    new_child_id = id_mapping.get(original_child_id)
    if new_parent_id is not None and new_child_id is not None:
        target_cursor.execute('''
        INSERT INTO tree_diversify_relations (parent_id, child_id, hitCount) 
        VALUES (?, ?, ?) 
        ON CONFLICT(parent_id, child_id) DO UPDATE SET hitCount = hitCount + ?
        ''', (new_parent_id, new_child_id, hitCount, hitCount))
print('关系导入完成...')

# 提交更改并关闭连接
source_conn.close()
target_conn.commit()
target_conn.close()
print('导入完毕...')