import pymysql
from fastmcp import FastMCP
from typing import Any, Optional
from bookstore_mcp.config import DB_CONFIG

mcp = FastMCP("BookStore")


def get_db_connection():
    """获取数据库连接"""
    config = DB_CONFIG.copy()
    return pymysql.connect(**config)


@mcp.tool()
def search_books_by_title(title_query: str, sessionId: Optional[str] = None, action: Optional[str] = None, chatInput: Optional[str] = None, toolCallId: Optional[str] = None):
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
def get_all_books(sessionId: Optional[str] = None, action: Optional[str] = None, chatInput: Optional[str] = None, toolCallId: Optional[str] = None):
    """获取书店里所有的图书列表"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        query = """
        SELECT id, title, author, price, description, 
               isbn, stock, publisher
        FROM book
        WHERE deleted = FALSE
        """
        cursor.execute(query)
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
        }@mcp.tool()
def search_books_by_author(author_query: str, sessionId: Optional[str] = None, action: Optional[str] = None, chatInput: Optional[str] = None, toolCallId: Optional[str] = None):
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

