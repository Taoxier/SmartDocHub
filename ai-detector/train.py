from transformers import AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments
import pandas as pd
import torch
from datasets import Dataset

# ====================== 1. 加载模型与Tokenizer ======================
model_name = "bert-base-chinese"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(model_name, num_labels=2)

# ====================== 2. 加载数据集 ======================
# 文件名必须是 ai_detect_data.csv，和代码同一文件夹
df = pd.read_csv("ai_detect_data.csv")
dataset = Dataset.from_pandas(df)

# ====================== 3. 数据编码（分词） ======================
def tokenize_function(examples):
    return tokenizer(
        examples["text"],
        padding="max_length",
        truncation=True,
        max_length=256
    )

tokenized_dataset = dataset.map(tokenize_function, batched=True)

# ====================== 4. 训练参数 ======================
training_args = TrainingArguments(
    output_dir="./my_chinese_ai_detector",
    per_device_train_batch_size=4,
    num_train_epochs=3,
    learning_rate=2e-5,
    logging_steps=5,
    evaluation_strategy="no",
    fp16=False
)

# ====================== 5. 开始训练 ======================
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=tokenized_dataset,
)
trainer.train()

# ====================== 6. 保存训练好的模型 ======================
model.save_pretrained("./my_ai_detector")
tokenizer.save_pretrained("./my_ai_detector")
print("模型训练完成，已保存到 my_ai_detector 文件夹")