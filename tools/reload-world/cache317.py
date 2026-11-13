import struct, bz2, gzip, io
class Cache:
    def __init__(s, d):
        s.d=d; s.dat=open(d+'/main_file_cache.dat','rb').read()
        s.idx=[open(d+'/main_file_cache.idx%d'%i,'rb').read() for i in range(6)]
    def read(s, index, fid):
        ix=s.idx[index]
        if fid*6+6>len(ix): return None
        b=ix[fid*6:fid*6+6]
        size=int.from_bytes(b[:3],'big'); sector=int.from_bytes(b[3:],'big')
        if sector<=0: return None
        out=bytearray(); part=0
        while len(out)<size:
            if sector==0: return None
            h=s.dat[sector*520:sector*520+8]
            cid=int.from_bytes(h[0:2],'big'); cp=int.from_bytes(h[2:4],'big'); nxt=int.from_bytes(h[4:7],'big'); st=h[7]
            if cid!=(fid&0xffff) or cp!=part or st!=index+1: return None
            n=min(512,size-len(out)); out+=s.dat[sector*520+8:sector*520+8+n]; sector=nxt; part+=1
        return bytes(out)
def name_hash(n):
    h=0
    for c in n.upper(): h=(h*61+ord(c)-32)&0xffffffff
    return h
def unbz(b): return bz2.decompress(b'BZh1'+b)
class Archive:
    def __init__(s,data):
        dl=int.from_bytes(data[0:3],'big'); cl=int.from_bytes(data[3:6],'big')
        if dl!=cl: data=unbz(data[6:]); s.whole=True; p=0
        else: s.whole=False; p=6; 
        s.data=data
        n=int.from_bytes(data[p:p+2],'big'); p+=2
        off=p+n*10; s.files={}
        for i in range(n):
            h,dl,cl=struct.unpack('>IxHxH',b''.join([data[p:p+4],b'\0',data[p+5:p+7],b'\0',data[p+8:p+10]])) if False else (int.from_bytes(data[p:p+4],'big'),int.from_bytes(data[p+4:p+7],'big'),int.from_bytes(data[p+7:p+10],'big'))
            s.files[h]=(off,dl,cl); off+=cl; p+=10
    def get(s,name):
        off,dl,cl=s.files[name_hash(name)]
        raw=s.data[off:off+cl]
        return raw if s.whole else unbz(raw)
