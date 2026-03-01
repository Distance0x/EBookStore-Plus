import pymysql
from fastmcp import FastMCP
from typing import Optional
from bookstore_mcp.config import DB_CONFIG

mcp = FastMCP("BookStore")


def get_db_connection():
    """获取数据库连接"""
    config = DB_CONFIG.copy()
    return pymysql.connect(**config)


@mcp.tool()
def search_books_by_title(title_query: str):
    """根据书名搜索图书（模糊匹配）"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        query = """
        SELECT id, title, author, price, description, 
               isbn, stock, publisher
        FROM book
        WHERE deleted = FALSE AND title LIKE %s
        """
        cursor.execute(query, (f"%{title_query}%",))
        books = cursor.fetchall()
        
        cursor.close()
        conn.close()
        
        return {
            "success": True,
            "count": len(books),
            "books": books
        }
    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }


@mcp.tool()
def search_books_by_author(author_query: str):
    """根据作者搜索图书（模糊匹配）"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        query = """
        SELECT id, title, author, price, cover, description, 
               isbn, stock, publisher
        FROM book
        WHERE deleted = FALSE AND author LIKE %s
        """
        cursor.execute(query, (f"%{author_query}%",))
        books = cursor.fetchall()
        
        cursor.close()
        conn.close()
        
        return {
            "success": True,
            "count": len(books),
            "books": books
        }
    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }

