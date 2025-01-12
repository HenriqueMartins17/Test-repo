from langchain.text_splitter import RecursiveCharacterTextSplitter, TextSplitter


def get_text_splitter() -> TextSplitter:
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=256,
        chunk_overlap=30,
        length_function=len,
        is_separator_regex=False,
    )
    return text_splitter
