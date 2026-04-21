// 导入crypto-js库
import CryptoJS from "crypto-js";
// 加密工具函数
// 注意：使用encrypt函数需要引入CryptoJS库

/**
 * AES加密函数
 * @param {string} message - 要加密的消息
 * @param {string} keyStr - 加密密钥
 * @returns {string} 加密后的字符串
 */
export function encrypt(message, keyStr) {
  const key = CryptoJS.enc.Utf8.parse(keyStr);

  const encryptedData = CryptoJS.AES.encrypt(message, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  });

  return encryptedData.toString();
}
