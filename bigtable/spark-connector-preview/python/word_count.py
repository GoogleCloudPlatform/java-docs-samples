from pyspark.sql import SparkSession
import argparse

PROJECT_ID_PROPERTY_NAME = 'bigtableProjectId'
INSTANCE_ID_PROPERTY_NAME = 'bigtableInstanceId'
TABLE_NAME_PROPERTY_NAME = 'bigtableTableName'
CREATE_NEW_TABLE_PROPERTY_NAME = 'createNewTable'

parser = argparse.ArgumentParser()
parser.add_argument(
       '--' + PROJECT_ID_PROPERTY_NAME, help='Bigtable project ID.')
parser.add_argument(
       '--' + INSTANCE_ID_PROPERTY_NAME, help='Bigtable instance ID.')
parser.add_argument(
       '--' + TABLE_NAME_PROPERTY_NAME, help='Bigtable table name.')
args = vars(parser.parse_args())  # Convert args from Namespace to dict.

bigtable_project_id = args.get(PROJECT_ID_PROPERTY_NAME)
bigtable_instance_id = args.get(INSTANCE_ID_PROPERTY_NAME)
bigtable_table_name = args.get(TABLE_NAME_PROPERTY_NAME)
create_new_table = args.get(CREATE_NEW_TABLE_PROPERTY_NAME) or 'true'

if not (bigtable_project_id and
       bigtable_instance_id and
       bigtable_table_name):
       raise ValueError(
              f'Bigtable project ID, instance ID, and table id should be specified '
              f'using --{PROJECT_ID_PROPERTY_NAME}=X, --{INSTANCE_ID_PROPERTY_NAME}=Y, '
              f'and --{TABLE_NAME_PROPERTY_NAME}=Z, respectively.'
              )

spark = SparkSession.builder.getOrCreate()

catalog = ''.join(("""{
      "table":{"namespace":"default", "name":" """ + bigtable_table_name + """
       ", "tableCoder":"PrimitiveType"},
      "rowkey":"wordCol",
      "columns":{
        "word":{"cf":"rowkey", "col":"wordCol", "type":"string"},
        "count":{"cf":"example_family", "col":"countCol", "type":"int"}
      }
      }""").split())

data = [{'word': f'word{i}', 'count': i} for i in range(10)]
input_data = spark.createDataFrame(data)
print('Created the DataFrame:')
input_data.show()

input_data.write \
       .format('bigtable') \
       .options(catalog=catalog) \
       .option('spark.bigtable.project.id', bigtable_project_id) \
       .option('spark.bigtable.instance.id', bigtable_instance_id) \
       .option('spark.bigtable.create.new.table', create_new_table) \
       .save()
print('DataFrame was written to Bigtable.')

records = spark.read \
       .format('bigtable') \
       .option('spark.bigtable.project.id', bigtable_project_id) \
       .option('spark.bigtable.instance.id', bigtable_instance_id) \
       .options(catalog=catalog) \
       .load()

print('Reading the DataFrame from Bigtable:')
records.show()
